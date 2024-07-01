package com.linkly.libui;

public interface IUIStrings {
    String getPrompt(IUIDisplay.String_id promptId);
    String getPromptVA(IUIDisplay.String_id promptId, String... formatArgs);
}
