// Translation structure type - ensures all translations have the same keys
// but allows different string values for each language

export interface TranslationKeys {
  common: {
    login: string
    register: string
    logout: string
    cancel: string
    save: string
    confirm: string
    delete: string
    edit: string
    loading: string
    or: string
    tryAgain: string
    error: string
    welcome: string
    // Accessibility
    sortedDescending: string
    sortedAscending: string
    clickToSort: string
  }
  auth: {
    email: string
    emailPlaceholder: string
    password: string
    passwordPlaceholder: string
    passwordRequirements: string
    confirmPassword: string
    confirmPasswordPlaceholder: string
    nickname: string
    nicknamePlaceholder: string
    currentPassword: string
    currentPasswordPlaceholder: string
    newPassword: string
    newPasswordPlaceholder: string
    forgotPassword: string
    noAccount: string
    hasAccount: string
    loggingIn: string
    registering: string
    settingPassword: string
    changingPassword: string
    savingNickname: string
    rememberMe: string
  }
  login: {
    title: string
    subtitle: string
    success: string
    successMessage: string
    failed: string
    noCredential: string
  }
  register: {
    title: string
    subtitle: string
    success: string
    successMessage: string
    failed: string
  }
  myPage: {
    title: string
    role: string
    accountManagement: string
    favorites: string
    accountInfo: string
    passwordNotSet: string
    passwordSet: string
    setPassword: string
    changePassword: string
    passwordChangeDescription: string
    nicknameUpdated: string
    nicknameUpdatedMessage: string
    nicknameUpdateFailed: string
    passwordSetSuccess: string
    passwordSetSuccessMessage: string
    passwordSetFailed: string
    passwordChangeSuccess: string
    passwordChangeSuccessMessage: string
    passwordChangeFailed: string
    loginRequired: string
    loginRequiredMessage: string
    goToLogin: string
    noFavorites: string
    passwordSetButton: string
    passwordChangeButton: string
  }
  header: {
    nav: {
      home: string
      nickname: string
      generator: string
      myPage: string
    }
    menu: string
    login: string
    logout: string
    logoutSuccess: string
    logoutMessage: string
    logoutConfirmTitle: string
    logoutConfirmDescription: string
    logoutConfirmButton: string
    admin: string
    components: string
  }
  home: {
    noNicknames: string
    collectNicknamesHint: string
  }
  nickname: {
    tabs: {
      all: string
      lostArk: string
      mapleStory: string
      dictionary: string
    }
    filters: {
      game: string
      rarity: string
      search: string
      all: string
      gameAll: string
      rarityAll: string
      selectGame: string
      selectRarity: string
      searchPlaceholder: string
      clearAll: string
      gameModeOrTooltip: string
      gameModeAndTooltip: string
    }
    table: {
      nickname: string
      game: string
      rarity: string
      updatedAt: string
      server: string
      class: string
      itemLevel: string
      world: string
      job: string
      combatPower: string
      noResults: string
      noResultsDescription: string
      caption: string
    }
    loadMore: string
  }
  nav: {
    mypage: string
  }
  logout: {
    successMessage: string
  }
  footer: {
    home: string
    myPage: string
    termsOfService: string
    privacyPolicy: string
  }
  generator: {
    title: string
    subtitle: string
    generate: string
    copy: string
    copied: string
    disclaimer: string
  }
  notFound: {
    title: string
    subtitle: string
    backToHome: string
  }
  validation: {
    checkFields: string
  }
  errors: {
    INVALID_REQUEST: string
    DUPLICATE_EMAIL: string
    UNAUTHORIZED: string
    INVALID_CREDENTIALS: string
    INVALID_TOKEN: string
    FORBIDDEN: string
    NOT_FOUND: string
    USER_NOT_FOUND: string
    PASSWORD_MISMATCH: string
    PASSWORD_ALREADY_SET: string
    NO_PASSWORD_SET: string
    INVALID_CURRENT_PASSWORD: string
    API_TOKEN_NOT_FOUND: string
    // Rate limit errors
    TOO_MANY_REQUESTS: string
    ACCOUNT_LOCKED: string
    DEFAULT_ERROR: string
  }
}
