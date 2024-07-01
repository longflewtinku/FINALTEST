package com.linkly.payment.viewmodel.data;

import static com.linkly.libui.IUIDisplay.FRAG_TYPE.FRAG_NOT_SET;
import static com.linkly.libui.IUIDisplay.SCREEN_ICON.NO_ICON;
import static com.linkly.libui.IUIDisplay.SCREEN_ID.NOT_SET;
import static com.linkly.libui.IUIDisplay.String_id.STR_EMPTY;
import static com.linkly.payment.utilities.UIUtilities.getHintText;
import static com.linkly.payment.utilities.UIUtilities.getPromptText;
import static com.linkly.payment.utilities.UIUtilities.getTitleText;

import android.os.Bundle;

import androidx.lifecycle.MutableLiveData;

import com.linkly.kotlinandroidtoolkit.extensionfuncs.BundleExtensionsKt;
import com.linkly.libui.IUIDisplay;
import com.linkly.libui.display.DisplayFragmentOption;
import com.linkly.libui.display.DisplayQuestion;
import com.linkly.libui.display.DisplayRequest;
import com.linkly.libui.display.DisplayTableArray;
import com.pax.dal.entity.RSAPinKey;

import java.util.ArrayList;

/* Standard data for a fragment, data that does not need any manipulation */
public class UIFragData {

    /* Data that can be pretty much unpacked directly from the DisplayRequest */
    public MutableLiveData<String> title= new MutableLiveData<>();
    public MutableLiveData<String> prompt = new MutableLiveData<>();
    public MutableLiveData<IUIDisplay.String_id> titleId = new MutableLiveData<>();
    public MutableLiveData<IUIDisplay.String_id> promptId = new MutableLiveData<>();
    public MutableLiveData<IUIDisplay.String_id> buttonPromptId = new MutableLiveData<>();
    public MutableLiveData<IUIDisplay.SCREEN_ICON> infoIcon = new MutableLiveData<>();
    public MutableLiveData<IUIDisplay.SCREEN_ICON> infoIcon2 = new MutableLiveData<>();

    public MutableLiveData<String> hint = new MutableLiveData<>();
    public MutableLiveData<String> screenAmount = new MutableLiveData<>();
    public MutableLiveData<String> screenCurrency = new MutableLiveData<>();
    public MutableLiveData<String> screenBlankType = new MutableLiveData<>();
    public MutableLiveData<String> screenDefaultText = new MutableLiveData<>();
    public MutableLiveData<String>  buttonPrompt = new MutableLiveData<>();

    public MutableLiveData<Boolean> skipButtonOn = new MutableLiveData<>();
    public MutableLiveData<Boolean> enableBackButton = new MutableLiveData<>();
    public MutableLiveData<Boolean> disableScreensaver = new MutableLiveData<>();
    public MutableLiveData<Boolean> keepOnScreen = new MutableLiveData<>();
    public MutableLiveData<Boolean> userDismissible = new MutableLiveData<>();
    public MutableLiveData<Boolean> userSharable = new MutableLiveData<>();
    public MutableLiveData<Boolean> screenDisableBack = new MutableLiveData<>();
    public MutableLiveData<Boolean> userUpgrade = new MutableLiveData<>();

    public MutableLiveData<Integer> screenTimeout = new MutableLiveData<>();
    public MutableLiveData<Integer> minLen = new MutableLiveData<>();
    public MutableLiveData<Integer> maxLen = new MutableLiveData<>();
    public MutableLiveData<Integer> passwordMinLen = new MutableLiveData<>();
    public MutableLiveData<Integer> passwordMaxLen = new MutableLiveData<>();
    public MutableLiveData<Integer> uniqueId = new MutableLiveData<>();
    public MutableLiveData<Integer> presentCardTimeout = new MutableLiveData<>();

    public MutableLiveData<IUIDisplay.SCREEN_ID> screenId = new MutableLiveData<>();
    public MutableLiveData<IUIDisplay.FRAG_TYPE> fragType = new MutableLiveData<>();

    public MutableLiveData<ArrayList<DisplayQuestion>> screenOptionList = new MutableLiveData<>();
    public MutableLiveData<ArrayList<DisplayFragmentOption>> screenFragOptionList = new MutableLiveData<>();

    public MutableLiveData<DisplayTableArray> screenTableData = new MutableLiveData<>();
    public MutableLiveData<RSAPinKey> rsaPinKey = new MutableLiveData<>();
    public MutableLiveData<byte[]> QRBitmapData = new MutableLiveData<>();


    public UIFragData() {
        enableBackButton.setValue(false);
        keepOnScreen.setValue(false);
        userDismissible.setValue(false);
        userSharable.setValue(false);
        screenDisableBack.setValue(false);
        userUpgrade.setValue(false);

        buttonPrompt.setValue("");
        title.setValue("");
        prompt.setValue("");
        hint.setValue("");
        screenAmount.setValue("");
        screenCurrency.setValue("");
        screenBlankType.setValue("");
        screenDefaultText.setValue("");

        titleId.setValue(STR_EMPTY);
        promptId.setValue(STR_EMPTY);
        buttonPromptId.setValue(STR_EMPTY);

        infoIcon.setValue(NO_ICON);
        infoIcon.setValue(NO_ICON);

        fragType.setValue(FRAG_NOT_SET);

        screenTimeout.setValue(0);
        minLen.setValue(0);
        maxLen.setValue(0);
        passwordMinLen.setValue(0);
        passwordMaxLen.setValue(0);
        uniqueId.setValue(0);
        presentCardTimeout.setValue(0);


        screenId.setValue(NOT_SET);
        disableScreensaver.setValue(false);
        skipButtonOn.setValue(false);

        screenOptionList.setValue(new ArrayList<>());
        screenFragOptionList.setValue(new ArrayList<>());
        screenTableData.setValue(new DisplayTableArray());
        rsaPinKey.setValue(new RSAPinKey());
        QRBitmapData.setValue(null);


    }

    @SuppressWarnings("deprecation")
    public void setDisplayRequest(DisplayRequest newDisplayRequest) {

        if (newDisplayRequest == null)
            return;

        Bundle extras = newDisplayRequest.getUiExtras();

        title.setValue(getTitleText(newDisplayRequest));
        prompt.setValue(getPromptText(newDisplayRequest));
        hint.setValue(getHintText(newDisplayRequest));

        titleId.setValue((IUIDisplay.String_id)extras.get(IUIDisplay.uiTitleId));
        promptId.setValue((IUIDisplay.String_id)extras.get(IUIDisplay.uiPromptId));
        buttonPromptId.setValue((IUIDisplay.String_id)extras.get(IUIDisplay.uiButtonPromptId));

        infoIcon.setValue(BundleExtensionsKt.getSerializableCompat(
                extras, IUIDisplay.uiScreenIcon, IUIDisplay.SCREEN_ICON.class));
        infoIcon2.setValue(BundleExtensionsKt.getSerializableCompat(
                extras, IUIDisplay.uiScreenIcon2, IUIDisplay.SCREEN_ICON.class));
        fragType.setValue(BundleExtensionsKt.getSerializableCompat(
                extras, IUIDisplay.uiScreenFragType, IUIDisplay.FRAG_TYPE.class));

        skipButtonOn.setValue(extras.getBoolean(IUIDisplay.uiSkipButtonOn));
        disableScreensaver.setValue(extras.getBoolean(IUIDisplay.uiDisableScreensaver));
        enableBackButton.setValue(extras.getBoolean(IUIDisplay.uiEnableBackButton));
        keepOnScreen.setValue(extras.getBoolean(IUIDisplay.uiKeepOnScreen));
        userDismissible.setValue(extras.getBoolean(IUIDisplay.uiUserDismissible));
        userSharable.setValue(extras.getBoolean(IUIDisplay.uiUserSharable));
        screenDisableBack.setValue(extras.getBoolean(IUIDisplay.uiScreenDisableBack));
        userUpgrade.setValue(extras.getBoolean(IUIDisplay.uiUserUpgrade));

        screenAmount.setValue(extras.getString(IUIDisplay.uiScreenAmount));
        buttonPrompt.setValue(extras.getString(IUIDisplay.uiButtonPrompt));

        screenCurrency.setValue(extras.getString(IUIDisplay.uiScreenCurrency));
        screenBlankType.setValue(extras.getString(IUIDisplay.uiScreenBlankType));
        screenDefaultText.setValue(extras.getString(IUIDisplay.uiScreenDefaultText));

        screenTimeout.setValue(extras.getInt(IUIDisplay.uiScreenTimeout, 0));
        minLen.setValue(extras.getInt(IUIDisplay.uiScreenMinLen, 0));
        maxLen.setValue(extras.getInt(IUIDisplay.uiScreenMaxLen, 0));
        passwordMinLen.setValue(extras.getInt(IUIDisplay.uiPasswordMinLen, 0));
        passwordMaxLen.setValue(extras.getInt(IUIDisplay.uiPasswordMaxLen, 0));
        uniqueId.setValue(extras.getInt(IUIDisplay.uiUniqueId, 0));
        presentCardTimeout.setValue(extras.getInt(IUIDisplay.uiPresentCardTimeout, 0));

        screenId.setValue(BundleExtensionsKt.getSerializableCompat(
                extras, IUIDisplay.uiScreenID, IUIDisplay.SCREEN_ID.class));

        screenOptionList.setValue(extras.getParcelableArrayList(IUIDisplay.uiScreenOptionList));
        screenFragOptionList.setValue(extras.getParcelableArrayList(IUIDisplay.uiScreenFragOptionList));

        screenTableData.setValue(extras.getParcelable(IUIDisplay.uiScreenTableData));
        rsaPinKey.setValue(extras.getParcelable(IUIDisplay.uiRsaPinKey));
        QRBitmapData.setValue(extras.getByteArray(IUIDisplay.uiQRBitmapData));



    }

    public MutableLiveData<String> getTitle() {
        return this.title;
    }

    public MutableLiveData<String> getPrompt() {
        return this.prompt;
    }

    public MutableLiveData<IUIDisplay.String_id> getTitleId() {
        return this.titleId;
    }

    public MutableLiveData<IUIDisplay.String_id> getPromptId() {
        return this.promptId;
    }

    public MutableLiveData<IUIDisplay.String_id> getButtonPromptId() {
        return this.buttonPromptId;
    }

    public MutableLiveData<IUIDisplay.SCREEN_ICON> getInfoIcon() {
        return this.infoIcon;
    }

    public MutableLiveData<IUIDisplay.SCREEN_ICON> getInfoIcon2() {
        return this.infoIcon2;
    }

    public MutableLiveData<String> getHint() {
        return this.hint;
    }

    public MutableLiveData<String> getScreenAmount() {
        return this.screenAmount;
    }

    public MutableLiveData<String> getScreenCurrency() {
        return this.screenCurrency;
    }

    public MutableLiveData<String> getScreenBlankType() {
        return this.screenBlankType;
    }

    public MutableLiveData<String> getScreenDefaultText() {
        return this.screenDefaultText;
    }

    public MutableLiveData<String> getButtonPrompt() {
        return this.buttonPrompt;
    }

    public MutableLiveData<Boolean> getSkipButtonOn() {
        return this.skipButtonOn;
    }

    public MutableLiveData<Boolean> getEnableBackButton() {
        return this.enableBackButton;
    }

    public MutableLiveData<Boolean> getDisableScreensaver() {
        return this.disableScreensaver;
    }

    public MutableLiveData<Boolean> getKeepOnScreen() {
        return this.keepOnScreen;
    }

    public MutableLiveData<Boolean> getUserDismissible() {
        return this.userDismissible;
    }

    public MutableLiveData<Boolean> getUserSharable() {
        return this.userSharable;
    }

    public MutableLiveData<Boolean> getScreenDisableBack() {
        return this.screenDisableBack;
    }

    public MutableLiveData<Boolean> getUserUpgrade() {
        return this.userUpgrade;
    }

    public MutableLiveData<Integer> getScreenTimeout() {
        return this.screenTimeout;
    }

    public MutableLiveData<Integer> getMinLen() {
        return this.minLen;
    }

    public MutableLiveData<Integer> getMaxLen() {
        return this.maxLen;
    }

    public MutableLiveData<Integer> getPasswordMinLen() {
        return this.passwordMinLen;
    }

    public MutableLiveData<Integer> getPasswordMaxLen() {
        return this.passwordMaxLen;
    }

    public MutableLiveData<Integer> getUniqueId() {
        return this.uniqueId;
    }

    public MutableLiveData<Integer> getPresentCardTimeout() {
        return this.presentCardTimeout;
    }

    public MutableLiveData<IUIDisplay.SCREEN_ID> getScreenId() {
        return this.screenId;
    }

    public MutableLiveData<IUIDisplay.FRAG_TYPE> getFragType() {
        return this.fragType;
    }

    public MutableLiveData<ArrayList<DisplayQuestion>> getScreenOptionList() {
        return this.screenOptionList;
    }

    public MutableLiveData<ArrayList<DisplayFragmentOption>> getScreenFragOptionList() {
        return this.screenFragOptionList;
    }

    public MutableLiveData<DisplayTableArray> getScreenTableData() {
        return this.screenTableData;
    }

    public MutableLiveData<RSAPinKey> getRsaPinKey() {
        return this.rsaPinKey;
    }

    public MutableLiveData<byte[]> getQRBitmapData() {
        return this.QRBitmapData;
    }

    public void setTitle(MutableLiveData<String> title) {
        this.title = title;
    }

    public void setPrompt(MutableLiveData<String> prompt) {
        this.prompt = prompt;
    }

    public void setTitleId(MutableLiveData<IUIDisplay.String_id> titleId) {
        this.titleId = titleId;
    }

    public void setPromptId(MutableLiveData<IUIDisplay.String_id> promptId) {
        this.promptId = promptId;
    }

    public void setButtonPromptId(MutableLiveData<IUIDisplay.String_id> buttonPromptId) {
        this.buttonPromptId = buttonPromptId;
    }

    public void setInfoIcon(MutableLiveData<IUIDisplay.SCREEN_ICON> infoIcon) {
        this.infoIcon = infoIcon;
    }

    public void setInfoIcon2(MutableLiveData<IUIDisplay.SCREEN_ICON> infoIcon2) {
        this.infoIcon2 = infoIcon2;
    }

    public void setHint(MutableLiveData<String> hint) {
        this.hint = hint;
    }

    public void setScreenAmount(MutableLiveData<String> screenAmount) {
        this.screenAmount = screenAmount;
    }

    public void setScreenCurrency(MutableLiveData<String> screenCurrency) {
        this.screenCurrency = screenCurrency;
    }

    public void setScreenBlankType(MutableLiveData<String> screenBlankType) {
        this.screenBlankType = screenBlankType;
    }

    public void setScreenDefaultText(MutableLiveData<String> screenDefaultText) {
        this.screenDefaultText = screenDefaultText;
    }

    public void setButtonPrompt(MutableLiveData<String> buttonPrompt) {
        this.buttonPrompt = buttonPrompt;
    }

    public void setSkipButtonOn(MutableLiveData<Boolean> skipButtonOn) {
        this.skipButtonOn = skipButtonOn;
    }

    public void setEnableBackButton(MutableLiveData<Boolean> enableBackButton) {
        this.enableBackButton = enableBackButton;
    }

    public void setDisableScreensaver(MutableLiveData<Boolean> disableScreensaver) {
        this.disableScreensaver = disableScreensaver;
    }

    public void setKeepOnScreen(MutableLiveData<Boolean> keepOnScreen) {
        this.keepOnScreen = keepOnScreen;
    }

    public void setUserDismissible(MutableLiveData<Boolean> userDismissible) {
        this.userDismissible = userDismissible;
    }

    public void setUserSharable(MutableLiveData<Boolean> userSharable) {
        this.userSharable = userSharable;
    }

    public void setScreenDisableBack(MutableLiveData<Boolean> screenDisableBack) {
        this.screenDisableBack = screenDisableBack;
    }

    public void setUserUpgrade(MutableLiveData<Boolean> userUpgrade) {
        this.userUpgrade = userUpgrade;
    }

    public void setScreenTimeout(MutableLiveData<Integer> screenTimeout) {
        this.screenTimeout = screenTimeout;
    }

    public void setMinLen(MutableLiveData<Integer> minLen) {
        this.minLen = minLen;
    }

    public void setMaxLen(MutableLiveData<Integer> maxLen) {
        this.maxLen = maxLen;
    }

    public void setPasswordMinLen(MutableLiveData<Integer> passwordMinLen) {
        this.passwordMinLen = passwordMinLen;
    }

    public void setPasswordMaxLen(MutableLiveData<Integer> passwordMaxLen) {
        this.passwordMaxLen = passwordMaxLen;
    }

    public void setUniqueId(MutableLiveData<Integer> uniqueId) {
        this.uniqueId = uniqueId;
    }

    public void setPresentCardTimeout(MutableLiveData<Integer> presentCardTimeout) {
        this.presentCardTimeout = presentCardTimeout;
    }

    public void setScreenId(MutableLiveData<IUIDisplay.SCREEN_ID> screenId) {
        this.screenId = screenId;
    }

    public void setFragType(MutableLiveData<IUIDisplay.FRAG_TYPE> fragType) {
        this.fragType = fragType;
    }

    public void setScreenOptionList(MutableLiveData<ArrayList<DisplayQuestion>> screenOptionList) {
        this.screenOptionList = screenOptionList;
    }

    public void setScreenFragOptionList(MutableLiveData<ArrayList<DisplayFragmentOption>> screenFragOptionList) {
        this.screenFragOptionList = screenFragOptionList;
    }

    public void setScreenTableData(MutableLiveData<DisplayTableArray> screenTableData) {
        this.screenTableData = screenTableData;
    }

    public void setRsaPinKey(MutableLiveData<RSAPinKey> rsaPinKey) {
        this.rsaPinKey = rsaPinKey;
    }

    public void setQRBitmapData(MutableLiveData<byte[]> QRBitmapData) {
        this.QRBitmapData = QRBitmapData;
    }
}
