(function () {
  'use strict';

  const STORAGE_KEY = 'realcode_locale';
  const DEFAULT_LOCALE = 'en';
  const SUPPORTED_LOCALES = ['en', 'es', 'ja'];

  const dictionaries = {
    en: {
      'page.title.home': 'Realcode — Practice Platform',
      'page.title.problems': 'Problems — Realcode',
      'page.title.create': 'Create Problem — Realcode',
      'page.title.solve': 'Solve — Realcode',
      'page.title.login': 'Sign In — Realcode',
      'page.title.profile': 'Profile — Realcode',
      'nav.home': 'Home',
      'nav.problems': 'Problems',
      'nav.create': 'Create',
      'nav.login': 'Sign In',
      'nav.profile': 'Profile',
      'nav.languageLabel': 'Language',
      'nav.toggleNavigation': 'Toggle navigation',
      'languages.english': 'English',
      'languages.spanish': 'Spanish',
      'languages.japanese': 'Japanese',
      'common.loading': 'Loading...',
      'common.error': 'Error',
      'common.none': 'None',
      'common.unknownDate': 'Unknown date',
      'home.exploreProblems': 'Explore Problems →',
      'home.logOut': 'Log Out',
      'home.logInToContinue': 'Log In to Continue',
      'home.searchLabel': 'Home / Search',
      'home.creationLabel': 'Problem Creation',
      'problems.searchPlaceholder': 'Search by name',
      'problems.allDifficulties': 'All difficulties',
      'problems.difficultyEasy': 'Easy',
      'problems.difficultyMedium': 'Medium',
      'problems.difficultyHard': 'Hard',
      'problems.allLanguages': 'All languages',
      'problems.languagePython': 'Python',
      'problems.languageJavaScript': 'JavaScript',
      'problems.languageJava': 'Java',
      'problems.languageCpp': 'C++',
      'problems.sortDifficulty': 'Difficulty',
      'problems.sortAlphabetical': 'Alphabetical',
      'problems.sortCreationDate': 'Creation date',
      'problems.clearFilters': 'Clear filters',
      'problems.count': '{{count}} problems found',
      'problems.noMatch': 'No problems match your current search.',
      'problems.loading': 'Loading problems...',
      'problems.failedLoad': 'Failed to load problems.',
      'problems.invalidDataset': 'Problems dataset is invalid.',
      'problems.couldNotReachServer': 'Could not reach the server.',
      'problems.loadingDetails': 'Loading...',
      'problems.couldNotLoadDetails': 'Could not load problem details.',
      'problems.problemStatement': 'Problem Statement',
      'problems.input': 'Input',
      'problems.output': 'Output',
      'problems.constraints': 'Constraints',
      'problems.hints': 'Hints',
      'problems.examples': 'Examples',
      'problems.startCoding': 'Start Coding',
      'problems.languageNotSpecified': 'Language: Not specified',
      'problems.languageSingular': 'Language: {{language}}',
      'problems.languagePlural': 'Languages: {{languages}}',
      'problems.createdAt': 'Created {{date}}',
      'login.signIn': 'Sign In',
      'login.createAccount': 'Create Account',
      'login.email': 'Email',
      'login.password': 'Password',
      'login.username': 'Username',
      'login.noAccount': 'No account?',
      'login.alreadyHaveAccount': 'Already have an account?',
      'login.register': 'Register',
      'login.emailPasswordRequired': 'Email and password are required.',
      'login.invalidEmailPassword': 'Invalid email or password.',
      'login.allFieldsRequired': 'All fields are required.',
      'login.signingIn': 'Signing in...',
      'login.creatingAccount': 'Creating account...',
      'login.usernameOrEmailTaken': 'Username or email is already taken.',
      'login.registrationFailed': 'Registration failed. Please try again.',
      'login.couldNotReachServer': 'Could not reach the server. Is the backend running?',
      'create.title': 'Title',
      'create.description': 'Description',
      'create.difficulty': 'Difficulty',
      'create.selectDifficulty': 'Select…',
      'create.solutions': 'Solutions',
      'create.examples': 'Examples',
      'create.testCases': 'Test cases',
      'create.constraints': 'Constraints',
      'create.setupScriptOptional': 'Setup script (optional)',
      'create.solutionRunner': 'Solution Runner',
      'create.memoryLimit': 'Memory limit',
      'create.timeLimit': 'Time limit',
      'create.points': 'Points',
      'create.saveDraft': 'Save as Draft',
      'create.publish': 'Publish',
      'create.titlePlaceholder': 'e.g. Two Sum',
      'create.descriptionPlaceholder': 'Problem statement here...',
      'create.solutionsPlaceholder': 'Explain the optimal approach...',
      'create.inputPlaceholder': 'Input (e.g. nums = [2,7,11,15], target = 9)',
      'create.outputPlaceholder': 'Output (e.g. [0,1])',
      'create.explanationPlaceholder': 'Explanation (optional)',
      'create.testInputPlaceholder': 'Input',
      'create.testOutputPlaceholder': 'Expected Output',
      'create.constraintsPlaceholder': 'e.g. 2 <= nums.length <= 10^4',
      'create.setupCodePlaceholder': 'Setup code...',
      'create.setupOutputPlaceholder': 'Setup output...',
      'create.pythonRunnerPlaceholder': 'Python runner code...',
      'create.javaRunnerPlaceholder': 'Java runner code...',
      'create.pointsPlaceholder': '100',
      'create.mustBeLoggedIn': 'You must be logged in to create a problem.',
      'create.requiredFields': 'Title, statement and difficulty are required.',
      'create.publishing': 'Publishing...',
      'create.problemCreated': 'Problem "{{title}}" created successfully!',
      'create.sessionExpired': 'Session expired. Please log in again.',
      'create.conflict': 'Conflict: {{message}}',
      'create.problemAlreadyExists': 'A problem with that slug already exists.',
      'create.couldNotReachServer': 'Could not reach the server. Is the backend running?',
      'create.unexpectedError': 'An unexpected error occurred.',
      'profile.usedLanguages': 'Used Languages',
      'profile.activeStatus': 'Active',
      'profile.inProgress': 'In Progress',
      'profile.created': 'Created',
      'profile.submissions': 'Submissions',
      'profile.difficultyBreakdown': 'Difficulty Breakdown (Submissions)',
      'profile.none': 'None',
      'profile.languageFallback': 'Lang {{id}}',
      'solve.tabDescription': 'Description',
      'solve.tabExamples': 'Examples',
      'solve.tabHints': 'Hints',
      'solve.loadingProblem': 'Loading problem…',
      'solve.loading': 'Loading…',
      'solve.noHints': 'No hints available for this problem.',
      'solve.noExamples': 'No examples available.',
      'solve.example': 'Example {{number}}',
      'solve.inputLabel': 'Input',
      'solve.outputLabel': 'Output',
      'solve.descriptionTitle': 'Description',
      'solve.inputTitle': 'Input',
      'solve.outputTitle': 'Output',
      'solve.constraintsTitle': 'Constraints',
      'solve.hintsTitle': 'Hints',
      'solve.editorPlaceholder': '// Write your solution here…',
      'solve.runButton': '▶ Run',
      'solve.submitButton': 'Submit',
      'solve.outputTitle': 'Output',
      'solve.closeButton': 'Close',
      'solve.awaitingJudge': 'awaiting Judge0…',
      'solve.runStatus': 'Running',
      'solve.accepted': 'Accepted',
      'solve.wrongAnswer': 'Wrong Answer',
      'solve.runtimeError': 'Runtime Error',
      'solve.compileError': 'Compile Error',
      'solve.timeLimitExceeded': 'Time Limit Exceeded',
      'solve.memoryLimitExceeded': 'Memory Limit Exceeded',
      'solve.internalError': 'Internal Error',
      'solve.queued': 'Queued',
      'solve.timeMetric': 'time',
      'solve.memMetric': 'mem',
      'solve.passMetric': 'pass',
      'solve.executionFailed': 'Execution failed',
      'solve.cases': 'case',
    },
    es: {
      'page.title.home': 'Realcode — Plataforma de práctica',
      'page.title.problems': 'Problemas — Realcode',
      'page.title.create': 'Crear problema — Realcode',
      'page.title.solve': 'Resolver — Realcode',
      'page.title.login': 'Iniciar sesión — Realcode',
      'page.title.profile': 'Perfil — Realcode',
      'nav.home': 'Inicio',
      'nav.problems': 'Problemas',
      'nav.create': 'Crear',
      'nav.login': 'Iniciar sesión',
      'nav.profile': 'Perfil',
      'nav.languageLabel': 'Idioma',
      'nav.toggleNavigation': 'Alternar navegación',
      'languages.english': 'Inglés',
      'languages.spanish': 'Español',
      'languages.japanese': 'Japonés',
      'common.loading': 'Cargando...',
      'common.error': 'Error',
      'common.none': 'Ninguno',
      'common.unknownDate': 'Fecha desconocida',
      'home.exploreProblems': 'Explorar problemas →',
      'home.logOut': 'Cerrar sesión',
      'home.logInToContinue': 'Inicia sesión para continuar',
      'home.searchLabel': 'Inicio / Búsqueda',
      'home.creationLabel': 'Creación de problemas',
      'problems.searchPlaceholder': 'Buscar por nombre',
      'problems.allDifficulties': 'Todas las dificultades',
      'problems.difficultyEasy': 'Fácil',
      'problems.difficultyMedium': 'Media',
      'problems.difficultyHard': 'Difícil',
      'problems.allLanguages': 'Todos los lenguajes',
      'problems.languagePython': 'Python',
      'problems.languageJavaScript': 'JavaScript',
      'problems.languageJava': 'Java',
      'problems.languageCpp': 'C++',
      'problems.sortDifficulty': 'Dificultad',
      'problems.sortAlphabetical': 'Alfabético',
      'problems.sortCreationDate': 'Fecha de creación',
      'problems.clearFilters': 'Limpiar filtros',
      'problems.count': 'Se han encontrado {{count}} problemas',
      'problems.noMatch': 'No hay problemas que coincidan con tu búsqueda.',
      'problems.loading': 'Cargando problemas...',
      'problems.failedLoad': 'No se han podido cargar los problemas.',
      'problems.invalidDataset': 'El conjunto de problemas no es válido.',
      'problems.couldNotReachServer': 'No se ha podido conectar con el servidor.',
      'problems.loadingDetails': 'Cargando...',
      'problems.couldNotLoadDetails': 'No se han podido cargar los detalles del problema.',
      'problems.problemStatement': 'Enunciado',
      'problems.input': 'Entrada',
      'problems.output': 'Salida',
      'problems.constraints': 'Restricciones',
      'problems.hints': 'Pistas',
      'problems.examples': 'Ejemplos',
      'problems.startCoding': 'Empezar a programar',
      'problems.languageNotSpecified': 'Lenguaje: no especificado',
      'problems.languageSingular': 'Lenguaje: {{language}}',
      'problems.languagePlural': 'Lenguajes: {{languages}}',
      'problems.createdAt': 'Creado {{date}}',
      'login.signIn': 'Iniciar sesión',
      'login.createAccount': 'Crear cuenta',
      'login.email': 'Correo electrónico',
      'login.password': 'Contraseña',
      'login.username': 'Nombre de usuario',
      'login.noAccount': '¿No tienes cuenta?',
      'login.alreadyHaveAccount': '¿Ya tienes cuenta?',
      'login.register': 'Registrarse',
      'login.emailPasswordRequired': 'El correo electrónico y la contraseña son obligatorios.',
      'login.invalidEmailPassword': 'Correo electrónico o contraseña inválidos.',
      'login.allFieldsRequired': 'Todos los campos son obligatorios.',
      'login.signingIn': 'Iniciando sesión...',
      'login.creatingAccount': 'Creando cuenta...',
      'login.usernameOrEmailTaken': 'El nombre de usuario o el correo ya están en uso.',
      'login.registrationFailed': 'El registro ha fallado. Inténtalo de nuevo.',
      'login.couldNotReachServer': 'No se ha podido conectar con el servidor. ¿Está funcionando el backend?',
      'create.title': 'Título',
      'create.description': 'Descripción',
      'create.difficulty': 'Dificultad',
      'create.selectDifficulty': 'Selecciona…',
      'create.solutions': 'Soluciones',
      'create.examples': 'Ejemplos',
      'create.testCases': 'Casos de prueba',
      'create.constraints': 'Restricciones',
      'create.setupScriptOptional': 'Script de preparación (opcional)',
      'create.solutionRunner': 'Ejecución de solución',
      'create.memoryLimit': 'Límite de memoria',
      'create.timeLimit': 'Límite de tiempo',
      'create.points': 'Puntos',
      'create.saveDraft': 'Guardar borrador',
      'create.publish': 'Publicar',
      'create.titlePlaceholder': 'p. ej. Two Sum',
      'create.descriptionPlaceholder': 'Escribe aquí el enunciado...',
      'create.solutionsPlaceholder': 'Explica el enfoque óptimo...',
      'create.inputPlaceholder': 'Entrada (p. ej. nums = [2,7,11,15], target = 9)',
      'create.outputPlaceholder': 'Salida (p. ej. [0,1])',
      'create.explanationPlaceholder': 'Explicación (opcional)',
      'create.testInputPlaceholder': 'Entrada',
      'create.testOutputPlaceholder': 'Salida esperada',
      'create.constraintsPlaceholder': 'p. ej. 2 <= nums.length <= 10^4',
      'create.setupCodePlaceholder': 'Código de preparación...',
      'create.setupOutputPlaceholder': 'Salida de preparación...',
      'create.pythonRunnerPlaceholder': 'Código ejecutor en Python...',
      'create.javaRunnerPlaceholder': 'Código ejecutor en Java...',
      'create.pointsPlaceholder': '100',
      'create.mustBeLoggedIn': 'Debes iniciar sesión para crear un problema.',
      'create.requiredFields': 'El título, el enunciado y la dificultad son obligatorios.',
      'create.publishing': 'Publicando...',
      'create.problemCreated': '¡El problema "{{title}}" se ha creado correctamente!',
      'create.sessionExpired': 'La sesión ha expirado. Vuelve a iniciar sesión.',
      'create.conflict': 'Conflicto: {{message}}',
      'create.problemAlreadyExists': 'Ya existe un problema con ese slug.',
      'create.couldNotReachServer': 'No se ha podido conectar con el servidor. ¿Está funcionando el backend?',
      'create.unexpectedError': 'Ha ocurrido un error inesperado.',
      'profile.usedLanguages': 'Lenguajes usados',
      'profile.activeStatus': 'Activo',
      'profile.inProgress': 'En progreso',
      'profile.created': 'Creados',
      'profile.submissions': 'Envíos',
      'profile.difficultyBreakdown': 'Desglose de dificultad (envíos)',
      'profile.none': 'Ninguno',
      'profile.languageFallback': 'Lang {{id}}',
      'solve.tabDescription': 'Descripción',
      'solve.tabExamples': 'Ejemplos',
      'solve.tabHints': 'Pistas',
      'solve.loadingProblem': 'Cargando problema…',
      'solve.loading': 'Cargando…',
      'solve.noHints': 'No hay pistas disponibles para este problema.',
      'solve.noExamples': 'No hay ejemplos disponibles.',
      'solve.example': 'Ejemplo {{number}}',
      'solve.inputLabel': 'Entrada',
      'solve.outputLabel': 'Salida',
      'solve.descriptionTitle': 'Descripción',
      'solve.inputTitle': 'Entrada',
      'solve.outputTitle': 'Salida',
      'solve.constraintsTitle': 'Restricciones',
      'solve.hintsTitle': 'Pistas',
      'solve.editorPlaceholder': '// Escribe tu solución aquí…',
      'solve.runButton': '▶ Ejecutar',
      'solve.submitButton': 'Enviar',
      'solve.closeButton': 'Cerrar',
      'solve.awaitingJudge': 'esperando Judge0…',
      'solve.runStatus': 'Ejecutando',
      'solve.accepted': 'Aceptado',
      'solve.wrongAnswer': 'Respuesta incorrecta',
      'solve.runtimeError': 'Error de ejecución',
      'solve.compileError': 'Error de compilación',
      'solve.timeLimitExceeded': 'Límite de tiempo excedido',
      'solve.memoryLimitExceeded': 'Límite de memoria excedido',
      'solve.internalError': 'Error interno',
      'solve.queued': 'En cola',
      'solve.timeMetric': 'tiempo',
      'solve.memMetric': 'mem',
      'solve.passMetric': 'aprobado',
      'solve.executionFailed': 'La ejecución falló',
      'solve.cases': 'caso',
    },
    ja: {
      'page.title.home': 'Realcode — 練習プラットフォーム',
      'page.title.problems': '問題 — Realcode',
      'page.title.create': '問題作成 — Realcode',
      'page.title.solve': '解答 — Realcode',
      'page.title.login': 'サインイン — Realcode',
      'page.title.profile': 'プロフィール — Realcode',
      'nav.home': 'ホーム',
      'nav.problems': '問題',
      'nav.create': '作成',
      'nav.login': 'サインイン',
      'nav.profile': 'プロフィール',
      'nav.languageLabel': '言語',
      'nav.toggleNavigation': 'ナビゲーションを切り替え',
      'languages.english': '英語',
      'languages.spanish': 'スペイン語',
      'languages.japanese': '日本語',
      'common.loading': '読み込み中...',
      'common.error': 'エラー',
      'common.none': 'なし',
      'common.unknownDate': '不明な日付',
      'home.exploreProblems': '問題を探す →',
      'home.logOut': 'ログアウト',
      'home.logInToContinue': '続行するにはサインインしてください',
      'home.searchLabel': 'ホーム / 検索',
      'home.creationLabel': '問題作成',
      'problems.searchPlaceholder': '名前で検索',
      'problems.allDifficulties': 'すべての難易度',
      'problems.difficultyEasy': '簡単',
      'problems.difficultyMedium': '普通',
      'problems.difficultyHard': '難しい',
      'problems.allLanguages': 'すべての言語',
      'problems.languagePython': 'Python',
      'problems.languageJavaScript': 'JavaScript',
      'problems.languageJava': 'Java',
      'problems.languageCpp': 'C++',
      'problems.sortDifficulty': '難易度',
      'problems.sortAlphabetical': 'アルファベット順',
      'problems.sortCreationDate': '作成日',
      'problems.clearFilters': 'フィルターをクリア',
      'problems.count': '{{count}} 件の問題が見つかりました',
      'problems.noMatch': '現在の検索条件に一致する問題はありません。',
      'problems.loading': '問題を読み込み中...',
      'problems.failedLoad': '問題の読み込みに失敗しました。',
      'problems.invalidDataset': '問題データセットが無効です。',
      'problems.couldNotReachServer': 'サーバーに接続できませんでした。',
      'problems.loadingDetails': '読み込み中...',
      'problems.couldNotLoadDetails': '問題の詳細を読み込めませんでした。',
      'problems.problemStatement': '問題文',
      'problems.input': '入力',
      'problems.output': '出力',
      'problems.constraints': '制約',
      'problems.hints': 'ヒント',
      'problems.examples': '例',
      'problems.startCoding': 'コーディングを開始',
      'problems.languageNotSpecified': '言語: 未指定',
      'problems.languageSingular': '言語: {{language}}',
      'problems.languagePlural': '言語: {{languages}}',
      'problems.createdAt': '作成日 {{date}}',
      'login.signIn': 'サインイン',
      'login.createAccount': 'アカウント作成',
      'login.email': 'メールアドレス',
      'login.password': 'パスワード',
      'login.username': 'ユーザー名',
      'login.noAccount': 'アカウントがありませんか？',
      'login.alreadyHaveAccount': 'すでにアカウントをお持ちですか？',
      'login.register': '登録',
      'login.emailPasswordRequired': 'メールアドレスとパスワードは必須です。',
      'login.invalidEmailPassword': 'メールアドレスまたはパスワードが正しくありません。',
      'login.allFieldsRequired': 'すべての項目は必須です。',
      'login.signingIn': 'サインイン中...',
      'login.creatingAccount': 'アカウントを作成中...',
      'login.usernameOrEmailTaken': 'ユーザー名またはメールアドレスは既に使用されています。',
      'login.registrationFailed': '登録に失敗しました。もう一度お試しください。',
      'login.couldNotReachServer': 'サーバーに接続できませんでした。バックエンドは起動していますか？',
      'create.title': 'タイトル',
      'create.description': '説明',
      'create.difficulty': '難易度',
      'create.selectDifficulty': '選択…',
      'create.solutions': '解法',
      'create.examples': '例',
      'create.testCases': 'テストケース',
      'create.constraints': '制約',
      'create.setupScriptOptional': 'セットアップスクリプト（任意）',
      'create.solutionRunner': 'ソリューションランナー',
      'create.memoryLimit': 'メモリ制限',
      'create.timeLimit': '時間制限',
      'create.points': 'ポイント',
      'create.saveDraft': '下書きを保存',
      'create.publish': '公開',
      'create.titlePlaceholder': '例: Two Sum',
      'create.descriptionPlaceholder': 'ここに問題文を入力...',
      'create.solutionsPlaceholder': '最適な方針を説明...',
      'create.inputPlaceholder': '入力（例: nums = [2,7,11,15], target = 9）',
      'create.outputPlaceholder': '出力（例: [0,1]）',
      'create.explanationPlaceholder': '説明（任意）',
      'create.testInputPlaceholder': '入力',
      'create.testOutputPlaceholder': '期待される出力',
      'create.constraintsPlaceholder': '例: 2 <= nums.length <= 10^4',
      'create.setupCodePlaceholder': 'セットアップコード...',
      'create.setupOutputPlaceholder': 'セットアップ出力...',
      'create.pythonRunnerPlaceholder': 'Python 実行コード...',
      'create.javaRunnerPlaceholder': 'Java 実行コード...',
      'create.pointsPlaceholder': '100',
      'create.mustBeLoggedIn': '問題を作成するにはサインインが必要です。',
      'create.requiredFields': 'タイトル、問題文、難易度は必須です。',
      'create.publishing': '公開中...',
      'create.problemCreated': '問題「{{title}}」を作成しました。',
      'create.sessionExpired': 'セッションの有効期限が切れました。再度サインインしてください。',
      'create.conflict': '競合: {{message}}',
      'create.problemAlreadyExists': 'その slug の問題は既に存在します。',
      'create.couldNotReachServer': 'サーバーに接続できませんでした。バックエンドは起動していますか？',
      'create.unexpectedError': '予期しないエラーが発生しました。',
      'profile.usedLanguages': '使用した言語',
      'profile.activeStatus': '有効',
      'profile.inProgress': '進行中',
      'profile.created': '作成済み',
      'profile.submissions': '提出数',
      'profile.difficultyBreakdown': '難易度別内訳（提出）',
      'profile.none': 'なし',
      'profile.languageFallback': '言語 {{id}}',
      'solve.tabDescription': '説明',
      'solve.tabExamples': '例',
      'solve.tabHints': 'ヒント',
      'solve.loadingProblem': '問題を読み込み中…',
      'solve.loading': '読み込み中…',
      'solve.noHints': 'この問題に利用可能なヒントはありません。',
      'solve.noExamples': '利用可能な例がありません。',
      'solve.example': '例 {{number}}',
      'solve.inputLabel': '入力',
      'solve.outputLabel': '出力',
      'solve.descriptionTitle': '説明',
      'solve.inputTitle': '入力',
      'solve.outputTitle': '出力',
      'solve.constraintsTitle': '制約',
      'solve.hintsTitle': 'ヒント',
      'solve.editorPlaceholder': '// ここにソリューションを記入…',
      'solve.runButton': '▶ 実行',
      'solve.submitButton': '提出',
      'solve.closeButton': '閉じる',
      'solve.awaitingJudge': 'Judge0を待機中…',
      'solve.runStatus': '実行中',
      'solve.accepted': '正解',
      'solve.wrongAnswer': '不正解',
      'solve.runtimeError': '実行時エラー',
      'solve.compileError': 'コンパイルエラー',
      'solve.timeLimitExceeded': '時間制限超過',
      'solve.memoryLimitExceeded': 'メモリ制限超過',
      'solve.internalError': '内部エラー',
      'solve.queued': 'キュー内',
      'solve.timeMetric': '時間',
      'solve.memMetric': 'メモリ',
      'solve.passMetric': '合格',
      'solve.executionFailed': '実行に失敗しました',
      'solve.cases': 'ケース',
    },
  };

  let currentLocale = DEFAULT_LOCALE;

  function normalizeLocale(locale) {
    if (!locale || typeof locale !== 'string') {
      return DEFAULT_LOCALE;
    }

    const short = locale.toLowerCase().split('-')[0];
    return SUPPORTED_LOCALES.includes(short) ? short : DEFAULT_LOCALE;
  }

  function getStoredLocale() {
    try {
      return localStorage.getItem(STORAGE_KEY);
    } catch (_error) {
      return null;
    }
  }

  function setStoredLocale(locale) {
    try {
      localStorage.setItem(STORAGE_KEY, locale);
    } catch (_error) {
      // Ignore storage failures and keep the selected locale in memory.
    }
  }

  function interpolate(template, params) {
    return String(template).replace(/\{\{(\w+)\}\}/g, (_match, token) => {
      if (!params || !Object.prototype.hasOwnProperty.call(params, token)) {
        return '';
      }
      return String(params[token]);
    });
  }

  function getLocale() {
    return currentLocale;
  }

  function t(key, params) {
    const dictionary = dictionaries[currentLocale] || dictionaries[DEFAULT_LOCALE];
    const template = (dictionary && dictionary[key]) || (dictionaries[DEFAULT_LOCALE] && dictionaries[DEFAULT_LOCALE][key]) || key;
    return interpolate(template, params);
  }

  function translateNode(node) {
    if (!(node instanceof Element)) {
      return;
    }

    const key = node.getAttribute('data-i18n');
    if (key) {
      node.textContent = t(key);
    }

    const placeholderKey = node.getAttribute('data-i18n-placeholder');
    if (placeholderKey) {
      node.setAttribute('placeholder', t(placeholderKey));
    }

    const titleKey = node.getAttribute('data-i18n-title');
    if (titleKey) {
      node.setAttribute('title', t(titleKey));
    }

    const ariaKey = node.getAttribute('data-i18n-aria-label');
    if (ariaKey) {
      node.setAttribute('aria-label', t(ariaKey));
    }

    const altKey = node.getAttribute('data-i18n-alt');
    if (altKey) {
      node.setAttribute('alt', t(altKey));
    }

    const valueKey = node.getAttribute('data-i18n-value');
    if (valueKey) {
      node.setAttribute('value', t(valueKey));
    }
  }

  function applyPage(root) {
    const scope = root || document;
    if (!scope || !document.body) {
      return;
    }

    document.documentElement.lang = currentLocale;

    const titleKey = document.body.getAttribute('data-page-title-key');
    if (titleKey) {
      document.title = t(titleKey);
    }

    scope.querySelectorAll('[data-i18n], [data-i18n-placeholder], [data-i18n-title], [data-i18n-aria-label], [data-i18n-alt], [data-i18n-value]').forEach(translateNode);
  }

  function syncLocaleFromStorage() {
    const storedRawLocale = getStoredLocale();
    const browserLocale = normalizeLocale(navigator.language || navigator.userLanguage || DEFAULT_LOCALE);

    if (storedRawLocale) {
      currentLocale = normalizeLocale(storedRawLocale);
      return;
    }

    currentLocale = browserLocale || DEFAULT_LOCALE;
  }

  function setLocale(locale, options) {
    const nextLocale = normalizeLocale(locale);
    currentLocale = nextLocale;

    const shouldPersist = !options || options.persist !== false;
    if (shouldPersist) {
      setStoredLocale(nextLocale);
    }

    applyPage();
    document.dispatchEvent(new CustomEvent('realcode:localechange', { detail: { locale: nextLocale } }));
    return nextLocale;
  }

  function init() {
    syncLocaleFromStorage();
    applyPage();
  }

  window.i18n = {
    getLocale,
    setLocale,
    t,
    applyPage,
    init,
    locales: SUPPORTED_LOCALES.slice(),
  };

  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', init, { once: true });
  } else {
    init();
  }
})();
