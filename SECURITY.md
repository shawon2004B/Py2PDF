# Security Policy

## Secure File Sharing

Py2PDF implements several security best practices for file handling and sharing:

### FileProvider Implementation

- All file sharing uses `content://` URIs instead of `file://` URIs
- Authority is set to `${applicationId}.fileprovider` to ensure uniqueness
- File paths are restricted to app-specific directories
- Only necessary paths are exposed

### URI Permissions

- `FLAG_GRANT_READ_URI_PERMISSION` is used for temporary read access
- Permissions are automatically revoked when the receiving app is done
- No persistent access is granted

### File Validation

- All files are validated before operations:
  - File existence checks
  - Readability validation
  - File type validation (PDF extension)
  - Path validation

### Data Safety

- `data_extraction_rules.xml` enforces Android 12+ data safety standards
- Backup rules exclude sensitive data from backups
- No cleartext traffic is permitted in production

## Permission Handling

### Required Permissions

- `READ_EXTERNAL_STORAGE`: Read PDF files (handled via FileProvider on Android 11+)
- `WRITE_EXTERNAL_STORAGE`: Write temporary data if needed
- `READ_MEDIA_IMAGES`, `READ_MEDIA_VIDEO`: Android 13+ alternatives

### Runtime Permissions

- Permissions should be requested at runtime on Android 6.0+
- User can revoke permissions at any time
- App handles permission denials gracefully with error messages

## Resource Management

- All file descriptors are properly closed
- Bitmaps are recycled after use
- Coroutines are scoped to lifecycle
- No resource leaks in error conditions

## Reporting Security Issues

If you discover a security vulnerability, please email mdshawonshaa@gmail.com instead of using the issue tracker.
