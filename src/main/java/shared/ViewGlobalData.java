package shared;

import shared.party.PartyMember;

public interface ViewGlobalData {
	boolean hasPartyMembers();

	int getPartyMemberCount();

	PartyMember getPartyMember(int index);

	int getSelectedPartyMember();

	void setSelectedPartyMember(int index);

	default void moveSelectedPartyMemberUp() {
		int prevIndex = (getSelectedPartyMember() == 0 ? getPartyMemberCount() : getSelectedPartyMember()) - 1;
		setSelectedPartyMember(prevIndex);
	}

	default void moveSelectedPartyMemberDown() {
		int nextIndex = getSelectedPartyMember() + 1;
		if (nextIndex == getPartyMemberCount()) {
			nextIndex = 0;
		}
		setSelectedPartyMember(nextIndex);
	}
}
