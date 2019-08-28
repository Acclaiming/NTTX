package io.kurumi.ntt.td.model;

import io.kurumi.ntt.td.TdApi.*;


public class TChat {
	
	private BasicGroup basicGroup;
	private Supergroup superGroupOrChannel;

	public TChat(Supergroup superGroupOrChannel) {
		
		this.superGroupOrChannel = superGroupOrChannel;
		
	}

	public TChat(BasicGroup basicGroup) {
		
		this.basicGroup = basicGroup;
		
	}
	
	public boolean isPrivate() {
		
		return basicGroup == null && superGroupOrChannel == null;
		
	}
	
	public boolean isBasicGroup() {

		return basicGroup != null;

	}
	
	public boolean isSuperGroup() {

		return superGroupOrChannel != null && !superGroupOrChannel.isChannel;

	}
	
	public boolean isChannel() {

		return superGroupOrChannel != null && superGroupOrChannel.isChannel;

	}
	
	public BasicGroup basicGroup() { return basicGroup; }
	public Supergroup superGroup() { return superGroupOrChannel; }
	public Supergroup channel() { return superGroupOrChannel; }
	
}
