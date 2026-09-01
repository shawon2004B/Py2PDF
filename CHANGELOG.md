# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [1.0.0] - 2026-09-01

### Added
- Initial release of Py2PDF Android application
- PDF preview rendering using native `PdfRenderer` and Jetpack Compose
- Secure file sharing with FileProvider and `content://` URIs
- MVVM architecture with proper state management
- Full Kotlin null-safety implementation
- Comprehensive error handling and user feedback
- Material Design 3 theme with dynamic color support
- Resource lifecycle management and cleanup
- Proper handling of Android storage permissions (API 21-34)
- Documentation and build configuration

### Security
- FileProvider for secure PDF sharing
- URI permission flags for temporary access
- Data extraction rules for Android 12+ compliance
- No cleartext traffic in production
- Backup rules for sensitive data exclusion

### Technical
- Android API 21-34 support
- Jetpack Compose declarative UI
- Kotlin Coroutines for async operations
- Kotlin Flow for reactive state management
- AndroidX libraries for modern Android development
- Proper resource cleanup and memory management
