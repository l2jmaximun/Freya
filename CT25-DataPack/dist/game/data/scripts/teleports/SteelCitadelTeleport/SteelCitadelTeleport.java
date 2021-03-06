package teleports.SteelCitadelTeleport;

import ct25.xtreme.Config;
import ct25.xtreme.gameserver.instancemanager.GrandBossManager;
import ct25.xtreme.gameserver.instancemanager.ZoneManager;
import ct25.xtreme.gameserver.model.L2CommandChannel;
import ct25.xtreme.gameserver.model.L2Party;
import ct25.xtreme.gameserver.model.actor.L2Npc;
import ct25.xtreme.gameserver.model.actor.instance.L2PcInstance;
import ct25.xtreme.gameserver.model.quest.Quest;
import ct25.xtreme.gameserver.model.zone.type.L2BossZone;

public class SteelCitadelTeleport extends Quest
{
	private static final int BELETH = 29118;
	private static final int NAIA_CUBE = 32376;

	public SteelCitadelTeleport(final int questId, final String name, final String descr)
	{
		super(questId, name, descr);
		
		addStartNpc(NAIA_CUBE);
		addTalkId(NAIA_CUBE);
	}
	
	@Override
	public String onTalk(final L2Npc npc, final L2PcInstance player)
	{
		switch (npc.getId())
		{
			case NAIA_CUBE:
				if (GrandBossManager.getInstance().getBossStatus(BELETH) == 3)
					return "32376-02.htm";
				
				final L2CommandChannel channel = player.getParty() == null ? null : player.getParty().getCommandChannel();
				
				if (channel == null || channel.getChannelLeader().getObjectId() != player.getObjectId() || channel.getMemberCount() < Config.Beleth_Min_Players)
					return "32376-02a.htm";
				
				if (GrandBossManager.getInstance().getBossStatus(BELETH) > 0)
					return "32376-03.htm";
				
				final L2BossZone zone = (L2BossZone) ZoneManager.getInstance().getZoneById(12018);
				
				if (zone != null)
				{
					GrandBossManager.getInstance().setBossStatus(BELETH, 1);
					
					for (final L2Party party : channel.getPartys())
					{
						if (party == null)
							continue;
						
						for (final L2PcInstance pl : party.getPartyMembers())
							if (pl.isInsideRadius(npc.getX(), npc.getY(), npc.getZ(), 3000, true, false))
							{
								zone.allowPlayerEntry(pl, 30);
								pl.teleToLocation(16342, 209557, -9352, true);
							}
					}
				}
		}
		
		return null;
	}
	
	public static void main(final String[] args)
	{
		new SteelCitadelTeleport(-1, "SteelCitadelTeleport", "teleports");
	}
}
