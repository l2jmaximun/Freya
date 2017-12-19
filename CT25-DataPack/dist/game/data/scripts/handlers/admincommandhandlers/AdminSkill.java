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
package handlers.admincommandhandlers;

import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

import ct25.xtreme.Config;
import ct25.xtreme.gameserver.datatables.SkillTable;
import ct25.xtreme.gameserver.handler.IAdminCommandHandler;
import ct25.xtreme.gameserver.model.L2Clan;
import ct25.xtreme.gameserver.model.L2Object;
import ct25.xtreme.gameserver.model.L2Skill;
import ct25.xtreme.gameserver.model.actor.instance.L2PcInstance;
import ct25.xtreme.gameserver.network.SystemMessageId;
import ct25.xtreme.gameserver.network.serverpackets.NpcHtmlMessage;
import ct25.xtreme.gameserver.network.serverpackets.PledgeSkillList;
import ct25.xtreme.gameserver.network.serverpackets.SystemMessage;
import ct25.xtreme.util.StringUtil;

/**
 * This class handles following admin commands: - show_skills - remove_skills - skill_list - skill_index - add_skill - remove_skill - get_skills - reset_skills - give_all_skills - give_all_skills_fs - remove_all_skills - add_clan_skills - admin_setskill
 * @version $Revision: 1.2.4.7 $ $Date: 2005/04/11 10:06:02 $ Small fixes by Zoey76 05/03/2011
 */
public class AdminSkill implements IAdminCommandHandler
{
	private static Logger _log = Logger.getLogger(AdminSkill.class.getName());

	private static final String[] ADMIN_COMMANDS =
	{
		"admin_show_skills",
		"admin_remove_skills",
		"admin_skill_list",
		"admin_skill_index",
		"admin_add_skill",
		"admin_remove_skill",
		"admin_get_skills",
		"admin_reset_skills",
		"admin_give_all_skills",
		"admin_give_all_skills_fs",
		"admin_remove_all_skills",
		"admin_add_clan_skill",
		"admin_setskill"
	};

	private static L2Skill[] adminSkills;

	@Override
	public boolean useAdminCommand(final String command, final L2PcInstance activeChar)
	{
		if (activeChar == null || !activeChar.getPcAdmin().canUseAdminCommand())
			return false;

		if (command.equals("admin_show_skills"))
			showMainPage(activeChar);
		else if (command.startsWith("admin_remove_skills"))
			try
			{
				final String val = command.substring(20);
				removeSkillsPage(activeChar, Integer.parseInt(val));
			}
			catch (final StringIndexOutOfBoundsException e)
			{
			}
		else if (command.startsWith("admin_skill_list"))
			AdminHelpPage.showHelpPage(activeChar, "skills.htm");
		else if (command.startsWith("admin_skill_index"))
			try
			{
				final String val = command.substring(18);
				AdminHelpPage.showHelpPage(activeChar, "skills/" + val + ".htm");
			}
			catch (final StringIndexOutOfBoundsException e)
			{
			}
		else if (command.startsWith("admin_add_skill"))
			try
			{
				final String val = command.substring(15);
				adminAddSkill(activeChar, val);
			}
			catch (final Exception e)
			{
				activeChar.sendMessage("Usage: //add_skill <skill_id> <level>");
			}
		else if (command.startsWith("admin_remove_skill"))
			try
			{
				final String id = command.substring(19);
				final int idval = Integer.parseInt(id);
				adminRemoveSkill(activeChar, idval);
			}
			catch (final Exception e)
			{
				activeChar.sendMessage("Usage: //remove_skill <skill_id>");
			}
		else if (command.equals("admin_get_skills"))
			adminGetSkills(activeChar);
		else if (command.equals("admin_reset_skills"))
			adminResetSkills(activeChar);
		else if (command.equals("admin_give_all_skills"))
			adminGiveAllSkills(activeChar, false);
		else if (command.equals("admin_give_all_skills_fs"))
			adminGiveAllSkills(activeChar, true);
		else if (command.equals("admin_remove_all_skills"))
		{
			if (activeChar.getTarget() instanceof L2PcInstance)
			{
				final L2PcInstance player = (L2PcInstance) activeChar.getTarget();
				for (final L2Skill skill : player.getAllSkills())
					player.removeSkill(skill);
				activeChar.sendMessage("You removed all skills from " + player.getName());
				player.sendMessage("Admin removed all skills from you.");
				player.sendSkillList();
				player.broadcastUserInfo();
			}
		}
		else if (command.startsWith("admin_add_clan_skill"))
			try
			{
				final String[] val = command.split(" ");
				adminAddClanSkill(activeChar, Integer.parseInt(val[1]), Integer.parseInt(val[2]));
			}
			catch (final Exception e)
			{
				activeChar.sendMessage("Usage: //add_clan_skill <skill_id> <level>");
			}
		else if (command.startsWith("admin_setskill"))
		{
			final String[] split = command.split(" ");
			final int id = Integer.parseInt(split[1]);
			final int lvl = Integer.parseInt(split[2]);
			final L2Skill skill = SkillTable.getInstance().getInfo(id, lvl);
			activeChar.addSkill(skill);
			activeChar.sendSkillList();
			activeChar.sendMessage("You added yourself skill " + skill.getName() + "(" + id + ") level " + lvl);
		}
		return true;
	}

	/**
	 * This function will give all the skills that the target can learn at his/her level
	 * @param activeChar the gm char
	 * @param includedByFs 
	 */
	private void adminGiveAllSkills(final L2PcInstance activeChar, final boolean includedByFs)
	{
		final L2Object target = activeChar.getTarget();
		L2PcInstance player = null;
		if (target instanceof L2PcInstance)
			player = (L2PcInstance) target;
		else
		{
			activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.INCORRECT_TARGET));
			return;
		}
		// Notify player and admin
		activeChar.sendMessage("You gave " + player.giveAvailableSkills(includedByFs, true) + " skills to " + player.getName());
		player.sendSkillList();
	}

	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}

	private void removeSkillsPage(final L2PcInstance activeChar, int page)
	{ // TODO: Externalize HTML
		final L2Object target = activeChar.getTarget();
		L2PcInstance player = null;
		if (target instanceof L2PcInstance)
			player = (L2PcInstance) target;
		else
		{
			activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.TARGET_IS_INCORRECT));
			return;
		}

		final L2Skill[] skills = player.getAllSkills();

		final int maxSkillsPerPage = 10;
		int maxPages = skills.length / maxSkillsPerPage;
		if (skills.length > maxSkillsPerPage * maxPages)
			maxPages++;

		if (page > maxPages)
			page = maxPages;

		final int skillsStart = maxSkillsPerPage * page;
		int skillsEnd = skills.length;
		if (skillsEnd - skillsStart > maxSkillsPerPage)
			skillsEnd = skillsStart + maxSkillsPerPage;

		final NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
		final StringBuilder replyMSG = StringUtil.startAppend(500 + maxPages * 50 + (skillsEnd - skillsStart + 1) * 50, "<html><body>" + "<table width=260><tr>"
			+ "<td width=40><button value=\"Main\" action=\"bypass -h admin_admin\" width=40 height=15 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td>" + "<td width=180><center>Character Selection Menu</center></td>"
			+ "<td width=40><button value=\"Back\" action=\"bypass -h admin_show_skills\" width=40 height=15 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td>" + "</tr></table>" + "<br><br>"
			+ "<center>Editing <font color=\"LEVEL\">", player.getName(), "</font></center>" + "<br><table width=270><tr><td>Lv: ", String.valueOf(player.getLevel()), " ", player.getTemplate().className, "</td></tr></table>"
				+ "<br><table width=270><tr><td>Note: Dont forget that modifying players skills can</td></tr>" + "<tr><td>ruin the game...</td></tr></table>" + "<br><center>Click on the skill you wish to remove:</center>" + "<br>" + "<center><table width=270><tr>");

		for (int x = 0; x < maxPages; x++)
		{
			final int pagenr = x + 1;
			StringUtil.append(replyMSG, "<td><a action=\"bypass -h admin_remove_skills ", String.valueOf(x), "\">Page ", String.valueOf(pagenr), "</a></td>");
		}

		replyMSG.append("</tr></table></center>" + "<br><table width=270>" + "<tr><td width=80>Name:</td><td width=60>Level:</td><td width=40>Id:</td></tr>");

		for (int i = skillsStart; i < skillsEnd; i++)
			StringUtil.append(replyMSG, "<tr><td width=80><a action=\"bypass -h admin_remove_skill ", String.valueOf(skills[i].getId()), "\">", skills[i].getName(), "</a></td><td width=60>", String.valueOf(skills[i].getLevel()), "</td><td width=40>", String.valueOf(skills[i].getId()), "</td></tr>");

		replyMSG.append("</table>" + "<br><center><table>" + "Remove skill by ID :" + "<tr><td>Id: </td>" + "<td><edit var=\"id_to_remove\" width=110></td></tr>" + "</table></center>"
			+ "<center><button value=\"Remove skill\" action=\"bypass -h admin_remove_skill $id_to_remove\" width=110 height=15 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></center>"
			+ "<br><center><button value=\"Back\" action=\"bypass -h admin_current_player\" width=40 height=15 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></center>" + "</body></html>");
		adminReply.setHtml(replyMSG.toString());
		activeChar.sendPacket(adminReply);
	}

	private void showMainPage(final L2PcInstance activeChar)
	{
		final L2Object target = activeChar.getTarget();
		L2PcInstance player = null;
		if (target instanceof L2PcInstance)
			player = (L2PcInstance) target;
		else
		{
			activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.INCORRECT_TARGET));
			return;
		}
		final NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
		adminReply.setFile(activeChar.getHtmlPrefix(), "data/html/admin/charskills.htm");
		adminReply.replace("%name%", player.getName());
		adminReply.replace("%level%", String.valueOf(player.getLevel()));
		adminReply.replace("%class%", player.getTemplate().className);
		activeChar.sendPacket(adminReply);
	}

	private void adminGetSkills(final L2PcInstance activeChar)
	{
		final L2Object target = activeChar.getTarget();
		L2PcInstance player = null;
		if (target instanceof L2PcInstance)
			player = (L2PcInstance) target;
		else
		{
			activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.INCORRECT_TARGET));
			return;
		}
		if (player.getName().equals(activeChar.getName()))
			player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.CANNOT_USE_ON_YOURSELF));
		else
		{
			final L2Skill[] skills = player.getAllSkills();
			adminSkills = activeChar.getAllSkills();
			for (final L2Skill skill : adminSkills)
				activeChar.removeSkill(skill);
			for (final L2Skill skill : skills)
				activeChar.addSkill(skill, true);
			activeChar.sendMessage("You now have all the skills of " + player.getName() + ".");
			activeChar.sendSkillList();
		}
		showMainPage(activeChar);
	}

	private void adminResetSkills(final L2PcInstance activeChar)
	{
		final L2Object target = activeChar.getTarget();
		L2PcInstance player = null;
		if (target instanceof L2PcInstance)
			player = (L2PcInstance) target;
		else
		{
			activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.INCORRECT_TARGET));
			return;
		}
		if (adminSkills == null)
			activeChar.sendMessage("You must get the skills of someone in order to do this.");
		else
		{
			final L2Skill[] skills = player.getAllSkills();
			for (final L2Skill skill : skills)
				player.removeSkill(skill);
			for (final L2Skill skill : activeChar.getAllSkills())
				player.addSkill(skill, true);
			for (final L2Skill skill : skills)
				activeChar.removeSkill(skill);
			for (final L2Skill skill : adminSkills)
				activeChar.addSkill(skill, true);
			player.sendMessage("[GM]" + activeChar.getName() + " updated your skills.");
			activeChar.sendMessage("You now have all your skills back.");
			adminSkills = null;
			activeChar.sendSkillList();
			player.sendSkillList();
		}
		showMainPage(activeChar);
	}

	private void adminAddSkill(final L2PcInstance activeChar, final String val)
	{
		final L2Object target = activeChar.getTarget();
		L2PcInstance player = null;
		if (target instanceof L2PcInstance)
			player = (L2PcInstance) target;
		else
		{
			showMainPage(activeChar);
			activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.INCORRECT_TARGET));
			return;
		}
		final StringTokenizer st = new StringTokenizer(val);
		if (st.countTokens() != 2)
			showMainPage(activeChar);
		else
		{
			L2Skill skill = null;
			try
			{
				final String id = st.nextToken();
				final String level = st.nextToken();
				final int idval = Integer.parseInt(id);
				final int levelval = Integer.parseInt(level);
				skill = SkillTable.getInstance().getInfo(idval, levelval);
			}
			catch (final Exception e)
			{
				_log.log(Level.WARNING, "", e);
			}
			if (skill != null)
			{
				final String name = skill.getName();
				// Player's info.
				player.sendMessage("Admin gave you the skill " + name + ".");
				player.addSkill(skill, true);
				player.sendSkillList();
				// Admin info.
				activeChar.sendMessage("You gave the skill " + name + " to " + player.getName() + ".");
				if (Config.DEBUG)
					_log.fine("[GM]" + activeChar.getName() + " gave skill " + name + " to " + player.getName() + ".");
				activeChar.sendSkillList();
			}
			else
				activeChar.sendMessage("Error: there is no such skill.");
			showMainPage(activeChar); // Back to start
		}
	}

	private void adminRemoveSkill(final L2PcInstance activeChar, final int idval)
	{
		final L2Object target = activeChar.getTarget();
		L2PcInstance player = null;
		if (target instanceof L2PcInstance)
			player = (L2PcInstance) target;
		else
		{
			activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.INCORRECT_TARGET));
			return;
		}
		final L2Skill skill = SkillTable.getInstance().getInfo(idval, player.getSkillLevel(idval));
		if (skill != null)
		{
			final String skillname = skill.getName();
			player.sendMessage("Admin removed the skill " + skillname + " from your skills list.");
			player.removeSkill(skill);
			// Admin information
			activeChar.sendMessage("You removed the skill " + skillname + " from " + player.getName() + ".");
			if (Config.DEBUG)
				_log.fine("[GM]" + activeChar.getName() + " removed skill " + skillname + " from " + player.getName() + ".");
			activeChar.sendSkillList();
		}
		else
			activeChar.sendMessage("Error: there is no such skill.");
		removeSkillsPage(activeChar, 0); // Back to previous page
	}

	private void adminAddClanSkill(final L2PcInstance activeChar, final int id, final int level)
	{
		final L2Object target = activeChar.getTarget();
		L2PcInstance player = null;
		if (target instanceof L2PcInstance)
			player = (L2PcInstance) target;
		else
		{
			showMainPage(activeChar);
			activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.INCORRECT_TARGET));
			return;
		}
		if (!player.isClanLeader())
		{
			activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_IS_NOT_A_CLAN_LEADER).addString(player.getName()));
			showMainPage(activeChar);
			return;
		}
		if (id < 370 || id > 391 || level < 1 || level > 3)
		{
			activeChar.sendMessage("Usage: //add_clan_skill <skill_id> <level>");
			showMainPage(activeChar);
			return;
		}
		final L2Skill skill = SkillTable.getInstance().getInfo(id, level);
		if (skill != null)
		{
			final String skillname = skill.getName();
			final SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.CLAN_SKILL_S1_ADDED);
			sm.addSkillName(skill);
			player.sendPacket(sm);
			final L2Clan clan = player.getClan();
			clan.broadcastToOnlineMembers(sm);
			clan.addNewSkill(skill);
			activeChar.sendMessage("You gave the Clan Skill: " + skillname + " to the clan " + clan.getName() + ".");

			clan.broadcastToOnlineMembers(new PledgeSkillList(clan));
			for (final L2PcInstance member : clan.getOnlineMembers(0))
				member.sendSkillList();

			showMainPage(activeChar);
			return;
		}
		activeChar.sendMessage("Error: there is no such skill.");
		return;
	}

}