package io.kurumi.ntt.td.client;

import io.kurumi.ntt.td.TdApi;
import io.kurumi.ntt.td.TdApi.*;

public class TdListener implements ITdListener {

	public TdClient client;

	@Override
	public void onInit(TdClient client) {

		this.client = client;

	}

	@Override
	public void onEvent(TdApi.Object event) {

		if (event instanceof UpdateActiveNotifications) {

			onActiveNotifications((UpdateActiveNotifications)event);

		} else if (event instanceof UpdateAuthorizationState) {

			onAuthorizationState((UpdateAuthorizationState)event);

		} else if (event instanceof UpdateBasicGroup) {

			onBasicGroup((UpdateBasicGroup)event);

		} else if (event instanceof UpdateBasicGroupFullInfo) {

			onBasicGroupFullInfo((UpdateBasicGroupFullInfo)event);

		} else if (event instanceof UpdateCall) {

			onCall((UpdateCall)event);

		} else if (event instanceof UpdateChatDefaultDisableNotification) {

			onChatDefaultDisableNotification((UpdateChatDefaultDisableNotification)event);

		} else if (event instanceof UpdateChatDraftMessage) {

			onChatDraftMessage((UpdateChatDraftMessage)event);

		} else if (event instanceof UpdateChatIsMarkedAsUnread) {

			onChatIsMarkedAsUnread((UpdateChatIsMarkedAsUnread)event);

		} else if (event instanceof UpdateChatIsPinned) {

			onChatIsPinned((UpdateChatIsPinned)event);

		} else if (event instanceof UpdateChatIsSponsored) {

			onChatIsSponsored((UpdateChatIsSponsored)event);

		} else if (event instanceof UpdateChatLastMessage) {

			onChatLastMessage((UpdateChatLastMessage)event);

		} else if (event instanceof UpdateChatNotificationSettings) {

			onChatNotificationSettings((UpdateChatNotificationSettings)event);

		} else if (event instanceof UpdateChatOnlineMemberCount) {

			onChatOnlineMemberCount((UpdateChatOnlineMemberCount)event);

		} else if (event instanceof UpdateChatOrder) {

			onChatOrder((UpdateChatOrder)event);

		} else if (event instanceof UpdateChatPhoto) {

			onChatPhoto((UpdateChatPhoto)event);

		} else if (event instanceof UpdateChatPinnedMessage) {

			onChatPinnedMessage((UpdateChatPinnedMessage)event);

		} else if (event instanceof UpdateChatReadInbox) {

			onChatReadInbox((UpdateChatReadInbox)event);

		} else if (event instanceof UpdateChatReadOutbox) {

			onChatReadOutbox((UpdateChatReadOutbox)event);

		} else if (event instanceof UpdateChatReplyMarkup) {

			onChatReplyMarkup((UpdateChatReplyMarkup)event);

		} else if (event instanceof UpdateChatTitle) {

			onChatTitle((UpdateChatTitle)event);

		} else if (event instanceof UpdateChatUnreadMentionCount) {

			onChatUnreadMentionCount((UpdateChatUnreadMentionCount)event);

		} else if (event instanceof UpdateConnectionState) {

			onConnectionState((UpdateConnectionState)event);

		} else if (event instanceof UpdateDeleteMessages) {

			onDeleteMessages((UpdateDeleteMessages)event);

		} else if (event instanceof UpdateFavoriteStickers) {

			onFavoriteStickers((UpdateFavoriteStickers)event);

		} else if (event instanceof UpdateFile) {

			onFile((UpdateFile)event);

		} else if (event instanceof UpdateFileGenerationStart) {

			onFileGenerationStart((UpdateFileGenerationStart)event);

		} else if (event instanceof UpdateFileGenerationStop) {

			onFileGenerationStop((UpdateFileGenerationStop)event);

		} else if (event instanceof UpdateHavePendingNotifications) {

			onHavePendingNotifications((UpdateHavePendingNotifications)event);

		} else if (event instanceof UpdateInstalledStickerSets) {

			onInstalledStickerSets((UpdateInstalledStickerSets)event);

		} else if (event instanceof UpdateLanguagePackStrings) {

			onLanguagePackStrings((UpdateLanguagePackStrings)event);

		} else if (event instanceof UpdateMessageContent) {

			onMessageContent((UpdateMessageContent)event);

		} else if (event instanceof UpdateMessageContentOpened) {

			onMessageContentOpened((UpdateMessageContentOpened)event);

		} else if (event instanceof UpdateMessageEdited) {

			onMessageEdited((UpdateMessageEdited)event);

		} else if (event instanceof UpdateMessageMentionRead) {

			onMessageMentionRead((UpdateMessageMentionRead)event);

		} else if (event instanceof UpdateMessageSendAcknowledged) {

			onMessageSendAcknowledged((UpdateMessageSendAcknowledged)event);

		} else if (event instanceof UpdateMessageSendFailed) {

			onMessageSendFailed((UpdateMessageSendFailed)event);

		} else if (event instanceof UpdateMessageSendSucceeded) {

			onMessageSendSucceeded((UpdateMessageSendSucceeded)event);

		} else if (event instanceof UpdateMessageViews) {

			onMessageViews((UpdateMessageViews)event);

		} else if (event instanceof UpdateNewCallbackQuery) {

			onNewCallbackQuery((UpdateNewCallbackQuery)event);

		} else if (event instanceof UpdateNewChat) {

			onNewChat((UpdateNewChat)event);

		} else if (event instanceof UpdateNewChosenInlineResult) {

			onNewChosenInlineResult((UpdateNewChosenInlineResult)event);

		} else if (event instanceof UpdateNewCustomEvent) {

			onNewCustomEvent((UpdateNewCustomEvent)event);

		} else if (event instanceof UpdateNewCustomQuery) {

			onNewCustomQuery((UpdateNewCustomQuery)event);

		} else if (event instanceof UpdateNewInlineCallbackQuery) {

			onNewInlineCallbackQuery((UpdateNewInlineCallbackQuery)event);

		} else if (event instanceof UpdateNewInlineQuery) {

			onNewInlineQuery((UpdateNewInlineQuery)event);

		} else if (event instanceof UpdateNewMessage) {

			onNewMessage((UpdateNewMessage)event);

		} else if (event instanceof UpdateNewPreCheckoutQuery) {

			onNewPreCheckoutQuery((UpdateNewPreCheckoutQuery)event);

		} else if (event instanceof UpdateNewShippingQuery) {

			onNewShippingQuery((UpdateNewShippingQuery)event);

		} else if (event instanceof UpdateNotification) {

			onNotification((UpdateNotification)event);

		} else if (event instanceof UpdateNotificationGroup) {

			onNotificationGroup((UpdateNotificationGroup)event);

		} else if (event instanceof UpdateOption) {

			onOption((UpdateOption)event);

		} else if (event instanceof UpdatePoll) {

			onPoll((UpdatePoll)event);

		} else if (event instanceof UpdateRecentStickers) {

			onRecentStickers((UpdateRecentStickers)event);

		} else if (event instanceof UpdateSavedAnimations) {

			onSavedAnimations((UpdateSavedAnimations)event);

		} else if (event instanceof UpdateScopeNotificationSettings) {

			onScopeNotificationSettings((UpdateScopeNotificationSettings)event);

		} else if (event instanceof UpdateSecretChat) {

			onSecretChat((UpdateSecretChat)event);

		} else if (event instanceof UpdateServiceNotification) {

			onServiceNotification((UpdateServiceNotification)event);

		} else if (event instanceof UpdateSupergroup) {

			onSupergroup((UpdateSupergroup)event);

		} else if (event instanceof UpdateSupergroupFullInfo) {

			onSupergroupFullInfo((UpdateSupergroupFullInfo)event);

		} else if (event instanceof UpdateTermsOfService) {

			onTermsOfService((UpdateTermsOfService)event);

		} else if (event instanceof UpdateTrendingStickerSets) {

			onTrendingStickerSets((UpdateTrendingStickerSets)event);

		} else if (event instanceof UpdateUnreadChatCount) {

			onUnreadChatCount((UpdateUnreadChatCount)event);

		} else if (event instanceof UpdateUnreadMessageCount) {

			onUnreadMessageCount((UpdateUnreadMessageCount)event);

		} else if (event instanceof UpdateUser) {

			onUser((UpdateUser)event);

		} else if (event instanceof UpdateUserChatAction) {

			onUserChatAction((UpdateUserChatAction)event);

		} else if (event instanceof UpdateUserFullInfo) {

			onUserFullInfo((UpdateUserFullInfo)event);

		} else if (event instanceof UpdateUserPrivacySettingRules) {

			onUserPrivacySettingRules((UpdateUserPrivacySettingRules)event);

		} else if (event instanceof UpdateUserStatus) {

			onUserStatus((UpdateUserStatus)event);
			
		}

	}
	
	public void onActiveNotifications(UpdateActiveNotifications update) {}

	public void onAuthorizationState(UpdateAuthorizationState update) {}

	public void onBasicGroup(UpdateBasicGroup update) {}

	public void onBasicGroupFullInfo(UpdateBasicGroupFullInfo update) {}

	public void onCall(UpdateCall update) {}

	public void onChatDefaultDisableNotification(UpdateChatDefaultDisableNotification update) {}

	public void onChatDraftMessage(UpdateChatDraftMessage update) {}

	public void onChatIsMarkedAsUnread(UpdateChatIsMarkedAsUnread update) {}

	public void onChatIsPinned(UpdateChatIsPinned update) {}

	public void onChatIsSponsored(UpdateChatIsSponsored update) {}

	public void onChatLastMessage(UpdateChatLastMessage update) {}

	public void onChatNotificationSettings(UpdateChatNotificationSettings update) {}

	public void onChatOnlineMemberCount(UpdateChatOnlineMemberCount update) {}

	public void onChatOrder(UpdateChatOrder update) {}

	public void onChatPhoto(UpdateChatPhoto update) {}

	public void onChatPinnedMessage(UpdateChatPinnedMessage update) {}

	public void onChatReadInbox(UpdateChatReadInbox update) {}

	public void onChatReadOutbox(UpdateChatReadOutbox update) {}

	public void onChatReplyMarkup(UpdateChatReplyMarkup update) {}

	public void onChatTitle(UpdateChatTitle update) {}

	public void onChatUnreadMentionCount(UpdateChatUnreadMentionCount update) {}

	public void onConnectionState(UpdateConnectionState update) {}

	public void onDeleteMessages(UpdateDeleteMessages update) {}

	public void onFavoriteStickers(UpdateFavoriteStickers update) {}

	public void onFile(UpdateFile update) {}

	public void onFileGenerationStart(UpdateFileGenerationStart update) {}

	public void onFileGenerationStop(UpdateFileGenerationStop update) {}

	public void onHavePendingNotifications(UpdateHavePendingNotifications update) {}

	public void onInstalledStickerSets(UpdateInstalledStickerSets update) {}

	public void onLanguagePackStrings(UpdateLanguagePackStrings update) {}

	public void onMessageContent(UpdateMessageContent update) {}

	public void onMessageContentOpened(UpdateMessageContentOpened update) {}

	public void onMessageEdited(UpdateMessageEdited update) {}

	public void onMessageMentionRead(UpdateMessageMentionRead update) {}

	public void onMessageSendAcknowledged(UpdateMessageSendAcknowledged update) {}

	public void onMessageSendFailed(UpdateMessageSendFailed update) {}

	public void onMessageSendSucceeded(UpdateMessageSendSucceeded update) {}

	public void onMessageViews(UpdateMessageViews update) {}

	public void onNewCallbackQuery(UpdateNewCallbackQuery update) {}

	public void onNewChat(UpdateNewChat update) {}

	public void onNewChosenInlineResult(UpdateNewChosenInlineResult update) {}

	public void onNewCustomEvent(UpdateNewCustomEvent update) {}

	public void onNewCustomQuery(UpdateNewCustomQuery update) {}

	public void onNewInlineCallbackQuery(UpdateNewInlineCallbackQuery update) {}

	public void onNewInlineQuery(UpdateNewInlineQuery update) {}

	public void onNewMessage(UpdateNewMessage update) {}

	public void onNewPreCheckoutQuery(UpdateNewPreCheckoutQuery update) {}

	public void onNewShippingQuery(UpdateNewShippingQuery update) {}

	public void onNotification(UpdateNotification update) {}

	public void onNotificationGroup(UpdateNotificationGroup update) {}

	public void onOption(UpdateOption update) {}

	public void onPoll(UpdatePoll update) {}

	public void onRecentStickers(UpdateRecentStickers update) {}

	public void onSavedAnimations(UpdateSavedAnimations update) {}

	public void onScopeNotificationSettings(UpdateScopeNotificationSettings update) {}

	public void onSecretChat(UpdateSecretChat update) {}

	public void onServiceNotification(UpdateServiceNotification update) {}

	public void onSupergroup(UpdateSupergroup update) {}

	public void onSupergroupFullInfo(UpdateSupergroupFullInfo update) {}

	public void onTermsOfService(UpdateTermsOfService update) {}

	public void onTrendingStickerSets(UpdateTrendingStickerSets update) {}

	public void onUnreadChatCount(UpdateUnreadChatCount update) {}

	public void onUnreadMessageCount(UpdateUnreadMessageCount update) {}

	public void onUser(UpdateUser update) {}

	public void onUserChatAction(UpdateUserChatAction update) {}

	public void onUserFullInfo(UpdateUserFullInfo update) {}

	public void onUserPrivacySettingRules(UpdateUserPrivacySettingRules update) {}

	public void onUserStatus(UpdateUserStatus update) {}
	

}
