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
package quests.Q00612_BattleAgainstKetraOrcs;

import java.util.HashMap;
import java.util.Map;

import ct25.xtreme.gameserver.model.actor.L2Npc;
import ct25.xtreme.gameserver.model.actor.instance.L2PcInstance;
import ct25.xtreme.gameserver.model.quest.Quest;
import ct25.xtreme.gameserver.model.quest.QuestState;
import ct25.xtreme.gameserver.model.quest.State;

/**
 * Battle against Ketra Orcs (612)
 * @author malyelfik
 */
public class Q00612_BattleAgainstKetraOrcs extends Quest
{
	// NPC
	private static final int ASHAS = 31377;
	// Monsters
	private static final Map<Integer, Integer> MOBS = new HashMap<>();
	static
	{
		MOBS.put(21324, 500); // Ketra Orc Footman
		MOBS.put(21327, 510); // Ketra Orc Raider
		MOBS.put(21328, 522); // Ketra Orc Scout
		MOBS.put(21329, 519); // Ketra Orc Shaman
		MOBS.put(21331, 529); // Ketra Orc Warrior
		MOBS.put(21332, 529); // Ketra Orc Lieutenant
		MOBS.put(21334, 539); // Ketra Orc Medium
		MOBS.put(21336, 548); // Ketra Orc White Captain
		MOBS.put(21338, 558); // Ketra Orc Seer
		MOBS.put(21339, 568); // Ketra Orc General
		MOBS.put(21340, 568); // Ketra Orc Battalion Commander
		MOBS.put(21342, 578); // Ketra Orc Grand Seer
		MOBS.put(21343, 664); // Ketra Commander
		MOBS.put(21345, 713); // Ketra's Head Shaman
		MOBS.put(21347, 738); // Ketra Prophet
	}
	// Items
	private static final int SEED = 7187;
	private static final int MOLAR = 7234;
	// Misc
	private static final int MIN_LEVEL = 74;
	private static final int MOLAR_COUNT = 100;

	private Q00612_BattleAgainstKetraOrcs(final int questId, final String name, final String descr)
	{
		super(questId, name, descr);
		addStartNpc(ASHAS);
		addTalkId(ASHAS);
		for (final int id : MOBS.keySet())
			super.addKillId(id);
		registerQuestItems(MOLAR);
	}

	@Override
	public String onAdvEvent(final String event, final L2Npc npc, final L2PcInstance player)
	{
		final QuestState st = player.getQuestState(getName());
		if (st == null)
			return null;

		String htmltext = event;
		switch (event)
		{
			case "31377-03.htm":
				st.startQuest();
				break;
			case "31377-06.html":
				break;
			case "31377-07.html":
				if (st.getQuestItemsCount(MOLAR) < MOLAR_COUNT)
					return "31377-08.html";
				st.takeItems(MOLAR, MOLAR_COUNT);
				st.giveItems(SEED, 20);
				break;
			case "31377-09.html":
				st.exitQuest(true, true);
				break;
			default:
				htmltext = null;
				break;
		}
		return htmltext;
	}

	@Override
	public String onKill(final L2Npc npc, final L2PcInstance killer, final boolean isPet)
	{
		final L2PcInstance member = getRandomPartyMember(killer, 1);
		if (member != null && getRandom(1000) < MOBS.get(npc.getId()))
		{
			final QuestState st = member.getQuestState(getName());
			st.giveItems(MOLAR, 1);
			st.playSound(QuestSound.ITEMSOUND_QUEST_ITEMGET);
		}
		return super.onKill(npc, killer, isPet);
	}

	@Override
	public String onTalk(final L2Npc npc, final L2PcInstance player)
	{
		String htmltext = getNoQuestMsg(player);
		final QuestState st = player.getQuestState(getName());
		if (st == null)
			return htmltext;

		switch (st.getState())
		{
			case State.CREATED:
				htmltext = player.getLevel() >= MIN_LEVEL ? "31377-01.htm" : "31377-02.htm";
				break;
			case State.STARTED:
				htmltext = st.hasQuestItems(MOLAR) ? "31377-04.html" : "31377-05.html";
				break;
		}
		return htmltext;
	}

	public static void main(final String[] args)
	{
		new Q00612_BattleAgainstKetraOrcs(612, Q00612_BattleAgainstKetraOrcs.class.getSimpleName(), "Battle against Ketra Orcs");
	}
}