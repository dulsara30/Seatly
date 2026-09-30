package com.seatly.backend.common.type;

// Lets ModuleException carry any module's key without depending on that module.
public interface MessageKey {

    String getKey();
}
