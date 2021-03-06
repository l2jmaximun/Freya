/*
 * Copyright (C) 2004-2013 L2J DataPack
 *
 * This file is part of L2J DataPack.
 *
 * L2J DataPack is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * L2J DataPack is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package quests.Q10282_ToTheSeedOfAnnihilation;

import ct25.xtreme.gameserver.model.actor.L2Npc;
import ct25.xtreme.gameserver.model.actor.instance.L2PcInstance;
import ct25.xtreme.gameserver.model.quest.Quest;
import ct25.xtreme.gameserver.model.quest.QuestState;
import ct25.xtreme.gameserver.model.quest.State;

/**
 * To the Seed of Destruction (10269)<br>
 * Original Jython script by Gnacik 2010-08-13 Based on Freya PTS.
 * @author nonom
 */
public class Q10282_ToTheSeedOfAnnihilation extends Quest
{
	// NPCs
	private static final int KBALDIR = 32733;
	private static final int KLEMIS = 32734;
	// Item
	private static final int SOA_ORDERS = 15512;

	public Q10282_ToTheSeedOfAnnihilation(final int questId, final String name, final String descr)
	{
		super(questId, name, descr);
		addStartNpc(KBALDIR);
		addTalkId(KBALDIR);
		addTalkId(KLEMIS);
		questItemIds = new int[]
		{
			SOA_ORDERS
		};
	}

	@Override
	public String onAdvEvent(final String event, final L2Npc npc, final L2PcInstance player)
	{
		final String htmltext = event;
		final QuestState st = player.getQuestState(getName());
		if (st == null)
			return htmltext;

		switch (event)
		{
			case "32733-07.htm":
				st.startQuest();
				st.giveItems(SOA_ORDERS, 1);
				break;
			case "32734-02.htm":
				st.addExpAndSp(1148480, 99110);
				st.exitQuest(false);
				break;
		}
		return htmltext;
	}

	@Override
	public String onTalk(final L2Npc npc, final L2PcInstance player)
	{
		String htmltext = getNoQuestMsg(player);
		final QuestState st = player.getQuestState(getName());
		if (st == null)
			return htmltext;

		final int npcId = npc.getId();
		switch (st.getState())
		{
			case State.COMPLETED:
				if (npcId == KBALDIR)
					htmltext = "32733-09.htm";
				else if (npcId == KLEMIS)
					htmltext = "32734-03.htm";
				break;
			case State.CREATED:
				htmltext = player.getLevel() < 84 ? "32733-00.htm" : "32733-01.htm";
				break;
			case State.STARTED:
				if (st.isCond(1))
					if (npcId == KBALDIR)
						htmltext = "32733-08.htm";
					else if (npcId == KLEMIS)
						htmltext = "32734-01.htm";
				break;
		}
		return htmltext;
	}

	public static void main(final String[] args)
	{
		new Q10282_ToTheSeedOfAnnihilation(10282, Q10282_ToTheSeedOfAnnihilation.class.getSimpleName(), "To the Seed of Annihilation");
	}
}
