# Multi-Image Upload System

## Image Upload Infrastructure

- **Service Layer**: `ImageUploadService` handles multi-file uploads with validation and processing
- **Environment-Specific Storage**: Configurable upload paths via `app.upload.path` property and `UPLOAD_PATH` environment variable
- **Storage Structure**: Files organized as `{uploadPath}/{category}/{entityId}/{UUID}.{ext}` (e.g., `/uploads/stores/{storeId}/{UUID}.{ext}`)
- **File Validation**: JPG, JPEG, PNG only; max 10MB per file; automatic image resizing (max 1920x1080)
- **Metadata Storage**: `StoreImage` and `MenuImage` entities track original filename, file size, content type, display order
- **Security**: UUID-based filenames prevent collisions and directory traversal attacks

## Image Entity Management

- **Store Images**: `StoreImage` entity with types: MAIN, GALLERY, INTERIOR, EXTERIOR
- **Menu Images**: `MenuImage` entity with types: MAIN, GALLERY, INGREDIENT, DETAIL
- **Repository Pattern**: Three-layer pattern with `StoreImageRepository`/`MenuImageRepository`
- **Entity Relationships**: `@OneToMany` relationships with cascade and orphan removal
- **Helper Methods**: `getImagePaths()`, `getMainImage()`, `getGalleryImages()` for easy access

## Frontend Implementation

- **Multi-file Selection**: `<input type="file" multiple>` with drag-and-drop support
- **Real-time Previews**: Thumbnail generation before upload with remove functionality
- **Auto-upload**: Images upload immediately after selection with progress indicators
- **Error Handling**: Client-side validation and server-side error reporting
- **Current Images**: Grid display of existing images with individual delete capability

## Environment Configuration

- **Local Development**: `application-local.properties` with `/mnt/d/projects/topping/uploads` path
- **Production**: `application-prod.properties` with `/home/ourbalance_topping/uploads` path
- **Environment Override**: `UPLOAD_PATH` environment variable overrides profile defaults
- **Resource Handler**: `WebConfig` dynamically maps `/uploads/**` to environment-specific filesystem paths
- **Profile Activation**: Use `--spring.profiles.active=local|prod` for environment selection

## API Endpoints

- **Upload**: `POST /stores/upload-images` - Multi-file upload with image type selection
- **Delete**: `POST /stores/delete-image/{imageId}` - Individual image deletion
- **Access Control**: Role-based permissions (BUSINESS_OWNER/ADMIN only)
- **Response Format**: Standard `ApiResponseData` wrapper with success/error handling
- **Resource Serving**: Images served via `/uploads/{category}/{entityId}/{filename}` URLs

## File Upload & Environment Configuration Troubleshooting

- **Upload Path Configuration**: Use `UPLOAD_PATH` environment variable to override default paths
- **Profile-Specific Paths**: Local development (`/mnt/d/projects/topping/uploads`) vs Production (`/home/ourbalance_topping/uploads`)
- **Directory Creation**: Upload directories are created automatically with proper permissions
- **Resource Serving**: Images served via `/uploads/**` URL pattern mapped to filesystem paths
- **Security**: Path traversal protection and validation for upload/delete operations