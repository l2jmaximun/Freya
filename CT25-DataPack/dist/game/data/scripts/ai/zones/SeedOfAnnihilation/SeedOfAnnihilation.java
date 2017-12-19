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
package ai.zones.SeedOfAnnihilation;

import java.util.Calendar;
import java.util.Map;

import ct25.xtreme.gameserver.datatables.SkillTable;
import ct25.xtreme.gameserver.datatables.SpawnTable;
import ct25.xtreme.gameserver.instancemanager.ZoneManager;
import ct25.xtreme.gameserver.model.L2Spawn;
import ct25.xtreme.gameserver.model.actor.L2Character;
import ct25.xtreme.gameserver.model.actor.L2Npc;
import ct25.xtreme.gameserver.model.actor.instance.L2MonsterInstance;
import ct25.xtreme.gameserver.model.actor.instance.L2PcInstance;
import ct25.xtreme.gameserver.model.quest.Quest;
import ct25.xtreme.gameserver.model.zone.L2ZoneType;
import ct25.xtreme.gameserver.model.zone.type.L2EffectZone;
import ct25.xtreme.gameserver.util.Util;
import javolution.util.FastMap;

public class SeedOfAnnihilation extends Quest
{
	// Misc
	final private static String qn = "SeedOfAnnihilation";

	// Teleport Zones
	private static final Map<Integer, int[]> _teleportZones = new FastMap<>();

	// NPC
	private static final int ANNIHILATION_FURNACE = 18928;

	// Strength, Agility, Wisdom
	private static final int[] ZONE_BUFFS =
	{
		0,
		6443,
		6444,
		6442
	};
	private static final int[][] ZONE_BUFFS_LIST =
	{
		{
			1,
			2,
			3
		},
		{
			1,
			3,
			2
		},
		{
			2,
			1,
			3
		},
		{
			2,
			3,
			1
		},
		{
			3,
			2,
			1
		},
		{
			3,
			1,
			2
		}
	};

	// 0: Bistakon, 1: Reptilikon, 2: Cokrakon
	private final SeedRegion[] _regionsData = new SeedRegion[3];
	private Long _seedsNextStatusChange;

	private static class SeedRegion
	{
		public int[] elite_mob_ids;
		public int[][] minion_lists;
		public int buff_zone;
		public int[][] af_spawns;
		public L2Npc[] af_npcs = new L2Npc[2];
		public int activeBuff = 0;

		public SeedRegion(final int[] emi, final int[][] ml, final int bz, final int[][] as)
		{
			elite_mob_ids = emi;
			minion_lists = ml;
			buff_zone = bz;
			af_spawns = as;
		}
	}

	static
	{
		_teleportZones.put(60002, new int[]
		{
			-213175,
			182648,
			-10992
		});
		_teleportZones.put(60003, new int[]
		{
			-181217,
			186711,
			-10528
		});
		_teleportZones.put(60004, new int[]
		{
			-180211,
			182984,
			-15152
		});
		_teleportZones.put(60005, new int[]
		{
			-179275,
			186802,
			-10720
		});
	}

	public void loadSeedRegionData()
	{
		// Bistakon data
		_regionsData[0] = new SeedRegion(new int[]
		{
			22750,
			22751,
			22752,
			22753
		}, new int[][]
		{
			{
				22746,
				22746,
				22746
			},
			{
				22747,
				22747,
				22747
			},
			{
				22748,
				22748,
				22748
			},
			{
				22749,
				22749,
				22749
			}
		}, 60006, new int[][]
		{
			{
				-180450,
				185507,
				-10544,
				11632
			},
			{
				-180005,
				185489,
				-10544,
				11632
			}
		});

		// Reptilikon data
		_regionsData[1] = new SeedRegion(new int[]
		{
			22757,
			22758,
			22759
		}, new int[][]
		{
			{
				22754,
				22755,
				22756
			}
		}, 60007, new int[][]
		{
			{
				-179600,
				186998,
				-10704,
				11632
			},
			{
				-179295,
				186444,
				-10704,
				11632
			}
		});

		// Cokrakon data
		_regionsData[2] = new SeedRegion(new int[]
		{
			22763,
			22764,
			22765
		}, new int[][]
		{
			{
				22760,
				22760,
				22761
			},
			{
				22760,
				22760,
				22762
			},
			{
				22761,
				22761,
				22760
			},
			{
				22761,
				22761,
				22762
			},
			{
				22762,
				22762,
				22760
			},
			{
				22762,
				22762,
				22761
			}
		}, 60008, new int[][]
		{
			{
				-180971,
				186361,
				-10528,
				11632
			},
			{
				-180758,
				186739,
				-10528,
				11632
			}
		});
		int buffsNow = 0;
		final String var = loadGlobalQuestVar("SeedNextStatusChange");
		if (var.equalsIgnoreCase("") || Long.parseLong(var) < System.currentTimeMillis())
		{
			buffsNow = getRandom(ZONE_BUFFS_LIST.length);
			saveGlobalQuestVar("SeedBuffsList", String.valueOf(buffsNow));
			_seedsNextStatusChange = getNextSeedsStatusChangeTime();
			saveGlobalQuestVar("SeedNextStatusChange", String.valueOf(_seedsNextStatusChange));
		}
		else
		{
			_seedsNextStatusChange = Long.parseLong(var);
			buffsNow = Integer.parseInt(loadGlobalQuestVar("SeedBuffsList"));
		}
		for (int i = 0; i < _regionsData.length; i++)
			_regionsData[i].activeBuff = ZONE_BUFFS_LIST[buffsNow][i];
	}

	private Long getNextSeedsStatusChangeTime()
	{
		final Calendar reenter = Calendar.getInstance();
		reenter.set(Calendar.SECOND, 0);
		reenter.set(Calendar.MINUTE, 0);
		reenter.set(Calendar.HOUR_OF_DAY, 13);
		reenter.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
		if (reenter.getTimeInMillis() <= System.currentTimeMillis())
			reenter.add(Calendar.DAY_OF_MONTH, 7);
		return reenter.getTimeInMillis();
	}

	public SeedOfAnnihilation(final int questId, final String name, final String descr)
	{
		super(questId, name, descr);
		loadSeedRegionData();
		for (final int i : _teleportZones.keySet())
			addEnterZoneId(i);
		for (final SeedRegion element : _regionsData)
			for (final int elite_mob_id : element.elite_mob_ids)
				addSpawnId(elite_mob_id);
		addStartNpc(32739);
		addTalkId(32739);
		initialMinionsSpawn();
		startEffectZonesControl();
	}

	private void startEffectZonesControl()
	{
		for (int i = 0; i < _regionsData.length; i++)
		{
			for (int j = 0; j < _regionsData[i].af_spawns.length; j++)
			{
				_regionsData[i].af_npcs[j] = addSpawn(ANNIHILATION_FURNACE, _regionsData[i].af_spawns[j][0], _regionsData[i].af_spawns[j][1], _regionsData[i].af_spawns[j][2], _regionsData[i].af_spawns[j][3], false, 0);
				_regionsData[i].af_npcs[j].setDisplayEffect(_regionsData[i].activeBuff);
			}
			ZoneManager.getInstance().getZoneById(_regionsData[i].buff_zone, L2EffectZone.class).addSkill(ZONE_BUFFS[_regionsData[i].activeBuff], 1);
		}
		startQuestTimer("ChangeSeedsStatus", _seedsNextStatusChange - System.currentTimeMillis(), null, null);
	}

	private void initialMinionsSpawn()
	{
		L2MonsterInstance mob;
		for (final SeedRegion sr : _regionsData)
			for (final int npcId : sr.elite_mob_ids)
				for (final L2Spawn spawn : SpawnTable.getInstance().getSpawns(npcId))
				{
					mob = (L2MonsterInstance) spawn.getLastSpawn();
					if (mob != null)
						spawnGroupOfMinion(mob, sr.minion_lists[getRandom(sr.minion_lists.length)]);
				}
	}

	private void spawnGroupOfMinion(final L2MonsterInstance npc, final int[] mobIds)
	{
		for (final int mobId : mobIds)
			addMinion(npc, mobId);
	}

	@Override
	public String onSpawn(final L2Npc npc)
	{
		for (final SeedRegion element : _regionsData)
			if (Util.contains(element.elite_mob_ids, npc.getId()))
				spawnGroupOfMinion((L2MonsterInstance) npc, element.minion_lists[getRandom(element.minion_lists.length)]);
		return super.onSpawn(npc);
	}

	@Override
	public String onAdvEvent(final String event, final L2Npc npc, final L2PcInstance player)
	{
		if (event.equalsIgnoreCase("ChangeSeedsStatus"))
		{
			final int buffsNow = getRandom(ZONE_BUFFS_LIST.length);
			saveGlobalQuestVar("SeedBuffsList", String.valueOf(buffsNow));
			_seedsNextStatusChange = getNextSeedsStatusChangeTime();
			saveGlobalQuestVar("SeedNextStatusChange", String.valueOf(_seedsNextStatusChange));
			for (int i = 0; i < _regionsData.length; i++)
			{
				_regionsData[i].activeBuff = ZONE_BUFFS_LIST[buffsNow][i];

				for (final L2Npc af : _regionsData[i].af_npcs)
					af.setDisplayEffect(_regionsData[i].activeBuff);

				final L2EffectZone zone = ZoneManager.getInstance().getZoneById(_regionsData[i].buff_zone, L2EffectZone.class);
				zone.clearSkills();
				zone.addSkill(ZONE_BUFFS[_regionsData[i].activeBuff], 1);
			}
			startQuestTimer("ChangeSeedsStatus", _seedsNextStatusChange - System.currentTimeMillis(), null, null);
		}
		else if (event.equalsIgnoreCase("transform"))
			if (player.getFirstEffect(6408) != null)
				npc.showChatWindow(player, 2);
			else
			{
				npc.setTarget(player);
				npc.doCast(SkillTable.getInstance().getInfo(6408, 1));
				npc.doCast(SkillTable.getInstance().getInfo(6649, 1));
				npc.showChatWindow(player, 1);
			}
		return null;
	}

	@Override
	public String onKill(final L2Npc npc, final L2PcInstance killer, final boolean isPet)
	{
		return super.onKill(npc, killer, isPet);
	}

	@Override
	public String onEnterZone(final L2Character character, final L2ZoneType zone)
	{
		if (_teleportZones.containsKey(zone.getId()))
		{
			final int[] teleLoc = _teleportZones.get(zone.getId());
			character.teleToLocation(teleLoc[0], teleLoc[1], teleLoc[2]);
		}
		return super.onEnterZone(character, zone);
	}

	public static void main(final String[] args)
	{
		new SeedOfAnnihilation(-1, qn, "ai/zones");
	}
}