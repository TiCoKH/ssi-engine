package shared;

import shared.party.PartyMember;

public interface ViewGlobalData {
	boolean hasPartyMembers();

	int getPartyMemberCount();

	PartyMember getPartyMember(int index);
}
