# Py2PDF

A modern Android application for previewing and sharing PDF files using Jetpack Compose.

## Features

- **PDF Preview**: Render PDF pages as high-quality bitmaps using `PdfRenderer`
- **Secure Sharing**: Share PDFs using `FileProvider` with proper URI permissions
- **Modern UI**: Built entirely with Jetpack Compose and Material Design 3
- **Coroutine-based**: Non-blocking file operations using Kotlin Coroutines
- **Error Handling**: Comprehensive exception handling and user feedback

## Architecture

- **MVVM Pattern**: Clean separation of concerns with ViewModel
- **Repository Pattern**: Encapsulated data access logic
- **Use Cases**: Domain layer for business logic
- **Compose**: Declarative UI framework

## Permissions

The app requires the following permissions:
- `READ_EXTERNAL_STORAGE`: Read PDF files from device storage
- `WRITE_EXTERNAL_STORAGE`: Write temporary PDF data if needed
- `READ_MEDIA_IMAGES`, `READ_MEDIA_VIDEO`: Android 13+ alternatives

## Building

```bash
./gradlew build
./gradlew installDebug
```

## Testing

```bash
./gradlew test
./gradlew connectedAndroidTest
```

## Security Features

- **FileProvider**: All file sharing uses content:// URIs instead of file:// URIs
- **URI Permissions**: Proper `FLAG_GRANT_READ_URI_PERMISSION` flags for intent sharing
- **Resource Cleanup**: All file descriptors and bitmaps are properly closed/recycled

## Code Health

- ✅ Null safety with Kotlin
- ✅ Proper exception handling
- ✅ Coroutine-based async operations
- ✅ Resource lifecycle management
- ✅ Modern Android API usage (API 21+)
- ✅ Compose best practices
