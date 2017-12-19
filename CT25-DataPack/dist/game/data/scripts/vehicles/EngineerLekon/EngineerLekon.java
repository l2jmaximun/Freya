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
package vehicles.EngineerLekon;

import ct25.xtreme.gameserver.instancemanager.AirShipManager;
import ct25.xtreme.gameserver.model.actor.L2Npc;
import ct25.xtreme.gameserver.model.actor.instance.L2PcInstance;
import ct25.xtreme.gameserver.model.quest.Quest;
import ct25.xtreme.gameserver.network.SystemMessageId;
import ct25.xtreme.gameserver.network.serverpackets.SystemMessage;

public class EngineerLekon extends Quest
{
	private static final int LEKON = 32557;

	private static final int LICENSE = 13559;
	private static final int STARSTONE = 13277;
	private static final int LICENSE_COST = 10;

	private static final SystemMessage SM_NEED_CLANLVL5 = SystemMessage.getSystemMessage(SystemMessageId.THE_AIRSHIP_NEED_CLANLVL_5_TO_SUMMON);
	private static final SystemMessage SM_NO_PRIVS = SystemMessage.getSystemMessage(SystemMessageId.THE_AIRSHIP_NO_PRIVILEGES);
	private static final SystemMessage SM_LICENSE_ALREADY_ACQUIRED = SystemMessage.getSystemMessage(SystemMessageId.THE_AIRSHIP_SUMMON_LICENSE_ALREADY_ACQUIRED);

	@Override
	public String onAdvEvent(final String event, final L2Npc npc, final L2PcInstance player)
	{
		if ("license".equalsIgnoreCase(event))
		{
			if (player.getClan() == null || player.getClan().getLevel() < 5)
			{
				player.sendPacket(SM_NEED_CLANLVL5);
				return null;
			}
			if (!player.isClanLeader())
			{
				player.sendPacket(SM_NO_PRIVS);
				return null;
			}
			if (AirShipManager.getInstance().hasAirShipLicense(player.getClanId()))
			{
				player.sendPacket(SM_LICENSE_ALREADY_ACQUIRED);
				return null;
			}
			if (player.getInventory().getItemByItemId(LICENSE) != null)
			{
				player.sendPacket(SM_LICENSE_ALREADY_ACQUIRED);
				return null;
			}
			if (!player.destroyItemByItemId("AirShipLicense", STARSTONE, LICENSE_COST, npc, true))
				return null;

			player.addItem("AirShipLicense", LICENSE, 1, npc, true);
			return null;
		}
		return event;
	}

	@Override
	public String onFirstTalk(final L2Npc npc, final L2PcInstance player)
	{
		if (player.getQuestState(getName()) == null)
			newQuestState(player);

		return npc.getId() + ".htm";
	}

	public EngineerLekon(final int questId, final String name, final String descr)
	{
		super(questId, name, descr);
		addStartNpc(LEKON);
		addFirstTalkId(LEKON);
		addTalkId(LEKON);
	}

	public static void main(final String[] args)
	{
		new EngineerLekon(-1, EngineerLekon.class.getSimpleName(), "vehicles");
	}
}