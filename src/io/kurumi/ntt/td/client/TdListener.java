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

			onUpdateActiveNotifications((UpdateActiveNotifications)event);

		} else if (event instanceof UpdateAuthorizationState) {

			onUpdateAuthorizationState((UpdateAuthorizationState)event);

		} else if (event instanceof UpdateBasicGroup) {

			onUpdateBasicGroup((UpdateBasicGroup)event);

		} else if (event instanceof UpdateBasicGroupFullInfo) {

			onUpdateBasicGroupFullInfo((UpdateBasicGroupFullInfo)event);

		} else if (event instanceof UpdateCall) {

			onUpdateCall((UpdateCall)event);

		} else if (event instanceof UpdateChatDefaultDisableNotification) {

			onUpdateChatDefaultDisableNotification((UpdateChatDefaultDisableNotification)event);

		} else if (event instanceof UpdateChatDraftMessage) {

			onUpdateChatDraftMessage((UpdateChatDraftMessage)event);

		} else if (event instanceof UpdateChatIsMarkedAsUnread) {

			onUpdateChatIsMarkedAsUnread((UpdateChatIsMarkedAsUnread)event);

		} else if (event instanceof UpdateChatIsPinned) {

			onUpdateChatIsPinned((UpdateChatIsPinned)event);

		} else if (event instanceof UpdateChatIsSponsored) {

			onUpdateChatIsSponsored((UpdateChatIsSponsored)event);

		} else if (event instanceof UpdateChatLastMessage) {

			onUpdateChatLastMessage((UpdateChatLastMessage)event);

		} else if (event instanceof UpdateChatNotificationSettings) {

			onUpdateChatNotificationSettings((UpdateChatNotificationSettings)event);

		} else if (event instanceof UpdateChatOnlineMemberCount) {

			onUpdateChatOnlineMemberCount((UpdateChatOnlineMemberCount)event);

		} else if (event instanceof UpdateChatOrder) {

			onUpdateChatOrder((UpdateChatOrder)event);

		} else if (event instanceof UpdateChatPhoto) {

			onUpdateChatPhoto((UpdateChatPhoto)event);

		} else if (event instanceof UpdateChatPinnedMessage) {

			onUpdateChatPinnedMessage((UpdateChatPinnedMessage)event);

		} else if (event instanceof UpdateChatReadInbox) {

			onUpdateChatReadInbox((UpdateChatReadInbox)event);

		} else if (event instanceof UpdateChatReadOutbox) {

			onUpdateChatReadOutbox((UpdateChatReadOutbox)event);

		} else if (event instanceof UpdateChatReplyMarkup) {

			onUpdateChatReplyMarkup((UpdateChatReplyMarkup)event);

		} else if (event instanceof UpdateChatTitle) {

			onUpdateChatTitle((UpdateChatTitle)event);

		} else if (event instanceof UpdateChatUnreadMentionCount) {

			onUpdateChatUnreadMentionCount((UpdateChatUnreadMentionCount)event);

		} else if (event instanceof UpdateConnectionState) {

			onUpdateConnectionState((UpdateConnectionState)event);

		} else if (event instanceof UpdateDeleteMessages) {

			onUpdateDeleteMessages((UpdateDeleteMessages)event);

		} else if (event instanceof UpdateFavoriteStickers) {

			onUpdateFavoriteStickers((UpdateFavoriteStickers)event);

		} else if (event instanceof UpdateFile) {

			onUpdateFile((UpdateFile)event);

		} else if (event instanceof UpdateFileGenerationStart) {

			onUpdateFileGenerationStart((UpdateFileGenerationStart)event);

		} else if (event instanceof UpdateFileGenerationStop) {

			onUpdateFileGenerationStop((UpdateFileGenerationStop)event);

		} else if (event instanceof UpdateHavePendingNotifications) {

			onUpdateHavePendingNotifications((UpdateHavePendingNotifications)event);

		} else if (event instanceof UpdateInstalledStickerSets) {

			onUpdateInstalledStickerSets((UpdateInstalledStickerSets)event);

		} else if (event instanceof UpdateLanguagePackStrings) {

			onUpdateLanguagePackStrings((UpdateLanguagePackStrings)event);

		} else if (event instanceof UpdateMessageContent) {

			onUpdateMessageContent((UpdateMessageContent)event);

		} else if (event instanceof UpdateMessageContentOpened) {

			onUpdateMessageContentOpened((UpdateMessageContentOpened)event);

		} else if (event instanceof UpdateMessageEdited) {

			onUpdateMessageEdited((UpdateMessageEdited)event);

		} else if (event instanceof UpdateMessageMentionRead) {

			onUpdateMessageMentionRead((UpdateMessageMentionRead)event);

		} else if (event instanceof UpdateMessageSendAcknowledged) {

			onUpdateMessageSendAcknowledged((UpdateMessageSendAcknowledged)event);

		} else if (event instanceof UpdateMessageSendFailed) {

			onUpdateMessageSendFailed((UpdateMessageSendFailed)event);

		} else if (event instanceof UpdateMessageSendSucceeded) {

			onUpdateMessageSendSucceeded((UpdateMessageSendSucceeded)event);

		} else if (event instanceof UpdateMessageViews) {

			onUpdateMessageViews((UpdateMessageViews)event);

		} else if (event instanceof UpdateNewCallbackQuery) {

			onUpdateNewCallbackQuery((UpdateNewCallbackQuery)event);

		} else if (event instanceof UpdateNewChat) {

			onUpdateNewChat((UpdateNewChat)event);

		} else if (event instanceof UpdateNewChosenInlineResult) {

			onUpdateNewChosenInlineResult((UpdateNewChosenInlineResult)event);

		} else if (event instanceof UpdateNewCustomEvent) {

			onUpdateNewCustomEvent((UpdateNewCustomEvent)event);

		} else if (event instanceof UpdateNewCustomQuery) {

			onUpdateNewCustomQuery((UpdateNewCustomQuery)event);

		} else if (event instanceof UpdateNewInlineCallbackQuery) {

			onUpdateNewInlineCallbackQuery((UpdateNewInlineCallbackQuery)event);

		} else if (event instanceof UpdateNewInlineQuery) {

			onUpdateNewInlineQuery((UpdateNewInlineQuery)event);

		} else if (event instanceof UpdateNewMessage) {

			onUpdateNewMessage((UpdateNewMessage)event);

		} else if (event instanceof UpdateNewPreCheckoutQuery) {

			onUpdateNewPreCheckoutQuery((UpdateNewPreCheckoutQuery)event);

		} else if (event instanceof UpdateNewShippingQuery) {

			onUpdateNewShippingQuery((UpdateNewShippingQuery)event);

		} else if (event instanceof UpdateNotification) {

			onUpdateNotification((UpdateNotification)event);

		} else if (event instanceof UpdateNotificationGroup) {

			onUpdateNotificationGroup((UpdateNotificationGroup)event);

		} else if (event instanceof UpdateOption) {

			onUpdateOption((UpdateOption)event);

		} else if (event instanceof UpdatePoll) {

			onUpdatePoll((UpdatePoll)event);

		} else if (event instanceof UpdateRecentStickers) {

			onUpdateRecentStickers((UpdateRecentStickers)event);

		} else if (event instanceof UpdateSavedAnimations) {

			onUpdateSavedAnimations((UpdateSavedAnimations)event);

		} else if (event instanceof UpdateScopeNotificationSettings) {

			onUpdateScopeNotificationSettings((UpdateScopeNotificationSettings)event);

		} else if (event instanceof UpdateSecretChat) {

			onUpdateSecretChat((UpdateSecretChat)event);

		} else if (event instanceof UpdateServiceNotification) {

			onUpdateServiceNotification((UpdateServiceNotification)event);

		} else if (event instanceof UpdateSupergroup) {

			onUpdateSupergroup((UpdateSupergroup)event);

		} else if (event instanceof UpdateSupergroupFullInfo) {

			onUpdateSupergroupFullInfo((UpdateSupergroupFullInfo)event);

		} else if (event instanceof UpdateTermsOfService) {

			onUpdateTermsOfService((UpdateTermsOfService)event);

		} else if (event instanceof UpdateTrendingStickerSets) {

			onUpdateTrendingStickerSets((UpdateTrendingStickerSets)event);

		} else if (event instanceof UpdateUnreadChatCount) {

			onUpdateUnreadChatCount((UpdateUnreadChatCount)event);

		} else if (event instanceof UpdateUnreadMessageCount) {

			onUpdateUnreadMessageCount((UpdateUnreadMessageCount)event);

		} else if (event instanceof UpdateUser) {

			onUpdateUser((UpdateUser)event);

		} else if (event instanceof UpdateUserChatAction) {

			onUpdateUserChatAction((UpdateUserChatAction)event);

		} else if (event instanceof UpdateUserFullInfo) {

			onUpdateUserFullInfo((UpdateUserFullInfo)event);

		} else if (event instanceof UpdateUserPrivacySettingRules) {

			onUpdateUserPrivacySettingRules((UpdateUserPrivacySettingRules)event);

		} else if (event instanceof UpdateUserStatus) {

			onUpdateUserStatus((UpdateUserStatus)event);
			
		}

	}
	
	public void onUpdateActiveNotifications(UpdateActiveNotifications update) {}

	public void onUpdateAuthorizationState(UpdateAuthorizationState update) {}

	public void onUpdateBasicGroup(UpdateBasicGroup update) {}

	public void onUpdateBasicGroupFullInfo(UpdateBasicGroupFullInfo update) {}

	public void onUpdateCall(UpdateCall update) {}

	public void onUpdateChatDefaultDisableNotification(UpdateChatDefaultDisableNotification update) {}

	public void onUpdateChatDraftMessage(UpdateChatDraftMessage update) {}

	public void onUpdateChatIsMarkedAsUnread(UpdateChatIsMarkedAsUnread update) {}

	public void onUpdateChatIsPinned(UpdateChatIsPinned update) {}

	public void onUpdateChatIsSponsored(UpdateChatIsSponsored update) {}

	public void onUpdateChatLastMessage(UpdateChatLastMessage update) {}

	public void onUpdateChatNotificationSettings(UpdateChatNotificationSettings update) {}

	public void onUpdateChatOnlineMemberCount(UpdateChatOnlineMemberCount update) {}

	public void onUpdateChatOrder(UpdateChatOrder update) {}

	public void onUpdateChatPhoto(UpdateChatPhoto update) {}

	public void onUpdateChatPinnedMessage(UpdateChatPinnedMessage update) {}

	public void onUpdateChatReadInbox(UpdateChatReadInbox update) {}

	public void onUpdateChatReadOutbox(UpdateChatReadOutbox update) {}

	public void onUpdateChatReplyMarkup(UpdateChatReplyMarkup update) {}

	public void onUpdateChatTitle(UpdateChatTitle update) {}

	public void onUpdateChatUnreadMentionCount(UpdateChatUnreadMentionCount update) {}

	public void onUpdateConnectionState(UpdateConnectionState update) {}

	public void onUpdateDeleteMessages(UpdateDeleteMessages update) {}

	public void onUpdateFavoriteStickers(UpdateFavoriteStickers update) {}

	public void onUpdateFile(UpdateFile update) {}

	public void onUpdateFileGenerationStart(UpdateFileGenerationStart update) {}

	public void onUpdateFileGenerationStop(UpdateFileGenerationStop update) {}

	public void onUpdateHavePendingNotifications(UpdateHavePendingNotifications update) {}

	public void onUpdateInstalledStickerSets(UpdateInstalledStickerSets update) {}

	public void onUpdateLanguagePackStrings(UpdateLanguagePackStrings update) {}

	public void onUpdateMessageContent(UpdateMessageContent update) {}

	public void onUpdateMessageContentOpened(UpdateMessageContentOpened update) {}

	public void onUpdateMessageEdited(UpdateMessageEdited update) {}

	public void onUpdateMessageMentionRead(UpdateMessageMentionRead update) {}

	public void onUpdateMessageSendAcknowledged(UpdateMessageSendAcknowledged update) {}

	public void onUpdateMessageSendFailed(UpdateMessageSendFailed update) {}

	public void onUpdateMessageSendSucceeded(UpdateMessageSendSucceeded update) {}

	public void onUpdateMessageViews(UpdateMessageViews update) {}

	public void onUpdateNewCallbackQuery(UpdateNewCallbackQuery update) {}

	public void onUpdateNewChat(UpdateNewChat update) {}

	public void onUpdateNewChosenInlineResult(UpdateNewChosenInlineResult update) {}

	public void onUpdateNewCustomEvent(UpdateNewCustomEvent update) {}

	public void onUpdateNewCustomQuery(UpdateNewCustomQuery update) {}

	public void onUpdateNewInlineCallbackQuery(UpdateNewInlineCallbackQuery update) {}

	public void onUpdateNewInlineQuery(UpdateNewInlineQuery update) {}

	public void onUpdateNewMessage(UpdateNewMessage update) {}

	public void onUpdateNewPreCheckoutQuery(UpdateNewPreCheckoutQuery update) {}

	public void onUpdateNewShippingQuery(UpdateNewShippingQuery update) {}

	public void onUpdateNotification(UpdateNotification update) {}

	public void onUpdateNotificationGroup(UpdateNotificationGroup update) {}

	public void onUpdateOption(UpdateOption update) {}

	public void onUpdatePoll(UpdatePoll update) {}

	public void onUpdateRecentStickers(UpdateRecentStickers update) {}

	public void onUpdateSavedAnimations(UpdateSavedAnimations update) {}

	public void onUpdateScopeNotificationSettings(UpdateScopeNotificationSettings update) {}

	public void onUpdateSecretChat(UpdateSecretChat update) {}

	public void onUpdateServiceNotification(UpdateServiceNotification update) {}

	public void onUpdateSupergroup(UpdateSupergroup update) {}

	public void onUpdateSupergroupFullInfo(UpdateSupergroupFullInfo update) {}

	public void onUpdateTermsOfService(UpdateTermsOfService update) {}

	public void onUpdateTrendingStickerSets(UpdateTrendingStickerSets update) {}

	public void onUpdateUnreadChatCount(UpdateUnreadChatCount update) {}

	public void onUpdateUnreadMessageCount(UpdateUnreadMessageCount update) {}

	public void onUpdateUser(UpdateUser update) {}

	public void onUpdateUserChatAction(UpdateUserChatAction update) {}

	public void onUpdateUserFullInfo(UpdateUserFullInfo update) {}

	public void onUpdateUserPrivacySettingRules(UpdateUserPrivacySettingRules update) {}

	public void onUpdateUserStatus(UpdateUserStatus update) {}
	

}
