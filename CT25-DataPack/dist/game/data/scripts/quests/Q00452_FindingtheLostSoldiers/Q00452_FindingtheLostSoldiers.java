/*
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package quests.Q00452_FindingtheLostSoldiers;

import ct25.xtreme.gameserver.model.actor.L2Npc;
import ct25.xtreme.gameserver.model.actor.instance.L2PcInstance;
import ct25.xtreme.gameserver.model.quest.Quest;
import ct25.xtreme.gameserver.model.quest.QuestState;
import ct25.xtreme.gameserver.model.quest.QuestState.QuestType;
import ct25.xtreme.gameserver.model.quest.State;

/**
 * Finding the Lost Soldiers (452)
 * @author Gigiikun
 * @version 2012-08-10
 */
public class Q00452_FindingtheLostSoldiers extends Quest
{
	private static final int JAKAN = 32773;
	private static final int TAG_ID = 15513;
	private static final int[] SOLDIER_CORPSES =
	{
		32769,
		32770,
		32771,
		32772
	};

	public Q00452_FindingtheLostSoldiers(final int questId, final String name, final String descr)
	{
		super(questId, name, descr);
		addStartNpc(JAKAN);
		addTalkId(JAKAN);
		addTalkId(SOLDIER_CORPSES);
		registerQuestItems(TAG_ID);
	}

	@Override
	public String onAdvEvent(final String event, final L2Npc npc, final L2PcInstance player)
	{
		final QuestState st = player.getQuestState(getName());
		if (st == null)
			return getNoQuestMsg(player);

		String htmltext = event;

		if (npc.getId() == JAKAN)
		{
			if (event.equals("32773-3.htm"))
				st.startQuest();
		}
		else if (st.isCond(1))
		{
			if (getRandom(10) < 5)
				st.giveItems(TAG_ID, 1);
			else
				htmltext = "corpse-3.html";
			st.setCond(2, true);
			npc.deleteMe();
		}
		else
			htmltext = "corpse-3.html";
		return htmltext;
	}

	@Override
	public String onTalk(final L2Npc npc, final L2PcInstance player)
	{
		String htmltext = getNoQuestMsg(player);
		final QuestState st = player.getQuestState(getName());
		if (st == null)
			return htmltext;

		if (npc.getId() == JAKAN)
			switch (st.getState())
			{
				case State.CREATED:
					htmltext = player.getLevel() < 84 ? "32773-0.html" : "32773-1.htm";
					break;
				case State.STARTED:
					if (st.isCond(1))
						htmltext = "32773-4.html";
					else if (st.isCond(2))
					{
						htmltext = "32773-5.html";
						st.takeItems(TAG_ID, -1);
						st.giveAdena(95200, true);
						st.addExpAndSp(435024, 50366);
						st.exitQuest(QuestType.DAILY, true);
					}
					break;
				case State.COMPLETED:
					if (st.isNowAvailable())
					{
						st.setState(State.CREATED);
						htmltext = player.getLevel() < 84 ? "32773-0.html" : "32773-1.htm";
					}
					else
						htmltext = "32773-6.html";
					break;
			}
		else if (st.isCond(1))
			htmltext = "corpse-1.html";
		return htmltext;
	}

	public static void main(final String[] args)
	{
		new Q00452_FindingtheLostSoldiers(452, Q00452_FindingtheLostSoldiers.class.getSimpleName(), "Finding the Lost Soldiers");
	}
}
