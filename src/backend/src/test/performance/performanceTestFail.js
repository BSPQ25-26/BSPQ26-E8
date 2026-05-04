import http from "k6/http";
import { check } from "k6";
import { sleep } from "k6";
import { SharedArray } from 'k6/data';
import { htmlReport } from 'https://raw.githubusercontent.com/benc-uk/k6-reporter/latest/dist/bundle.js'
const BASE_URL = __ENV.BASE_URL || "http://backend:10000";
import sql from "k6/x/sql"
import driver from "k6/x/sql/driver/postgres";


export function handleSummary(data) {
    return {
        '/reports/summaryPerfFail.html': htmlReport(data),
    }
}

const users = new SharedArray('users',function() {
    return JSON.parse(open('./userstest.json'));
});

const connectionString = `postgres://${__ENV.POSTGRES_USER}:${__ENV.POSTGRES_PASSWORD}` +
    `@postgres:${__ENV.POSTGRES_PORT}/${__ENV.POSTGRES_DB}?sslmode=disable`;

const db = sql.open(driver,connectionString)




export const options = {
    vus: 50,
    iterations:500,
    thresholds  : {
        http_req_duration: ['p(95) < 30'],
        http_req_failed: ['rate < 0.01']

    }
};

function login(user) {
    const response = http.post(
        `${BASE_URL}/api/auth/login`,
        JSON.stringify({ email: user.email, password: user.password }),
        { headers: { "Content-Type": "application/json" } }
    );

    const ok = check(response, {
        'login successful': (r) => r.status === 200,
        'login returned access token': (r) => Boolean(r.json('accessToken')),
        'login returned token type': (r) => Boolean(r.json('tokenType'))
    });

    if (!ok) {
        return null;
    }

    return {
        tokenType: response.json('tokenType'),
        accessToken: response.json('accessToken')
    };
}


function listProblem(user){
    const responseRes= http.get(
        `${BASE_URL}/api/problems`,
    );


    check(responseRes, { 'list problems successful': (r) => r.status === 200 });
}


function createProblem(user, authHeader) {
    const uniqueSuffix = `${__VU}-${__ITER}-${Date.now()}`;
    const title = `Perf problem ${user.username} ${uniqueSuffix}`;
    const payload = {
        slug: `perf-${user.username}-${uniqueSuffix}`.toLowerCase(),
        title,
        statementMd: "Solve the example problem.",
        difficulty: "EASY"
    };

    const response = http.post(
        `${BASE_URL}/api/problems`,
        JSON.stringify(payload),
        { headers: { "Content-Type": "application/json", "Authorization": authHeader } }
    );


    check(response, { 'create problem successful': (r) => r.status === 201 });
}

export default function () {
    const user = users[(__VU - 1) % users.length];
    const tokens = login(user);
    if (!tokens) {
        return;
    }

    const authHeader = `${tokens.tokenType} ${tokens.accessToken}`;

    sleep(1);
    createProblem(user, authHeader);
    sleep(1);
    listProblem(user);
}

export function teardown(){
    db.exec("DELETE FROM PROBLEMS;")
    db.close()
}