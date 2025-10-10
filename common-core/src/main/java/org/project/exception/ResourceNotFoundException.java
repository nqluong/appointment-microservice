package org.project.exception;

public class ResourceNotFoundException extends CustomException{
    public ResourceNotFoundException(String resourceName) {
        super(ErrorCode.RESOURCE_NOT_FOUND,
                String.format("%s không tồn tại", resourceName), null);
    }

    public ResourceNotFoundException(String resourceName, Object id) {
        super(ErrorCode.RESOURCE_NOT_FOUND,
                String.format("%s với ID %s không tồn tại", resourceName, id), null);
    }
}
