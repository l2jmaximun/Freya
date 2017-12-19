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
package ct25.xtreme.gameserver.network.serverpackets;

import ct25.xtreme.gameserver.model.actor.instance.L2PcInstance;

/**
 * This class ...
 *
 * @version $Revision: 1.3.2.1.2.3 $ $Date: 2005/03/27 15:29:57 $
 */
public final class PartySmallWindowDelete extends L2GameServerPacket
{
	private static final String _S__66_PARTYSMALLWINDOWDELETE = "[S] 51 PartySmallWindowDelete";
	private L2PcInstance _member;
	
	public PartySmallWindowDelete(L2PcInstance member)
	{
		_member = member;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0x51);
		writeD(_member.getObjectId());
		writeS(_member.getName());
	}
	
	/* (non-Javadoc)
	 * @see ct25.xtreme.gameserver.serverpackets.ServerBasePacket#getType()
	 */
	@Override
	public String getType()
	{
		return _S__66_PARTYSMALLWINDOWDELETE;
	}
}
