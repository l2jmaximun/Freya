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

/**
 * Format: ch
 * Trigger packet
 * @author  KenM
 */
public class ExShowVariationCancelWindow extends L2GameServerPacket
{
	private static final String _S__FE_51_EXSHOWVARIATIONCANCELWINDOW = "[S] FE:52 ExShowVariationCancelWindow";
	
	/**
	 * @see ct25.xtreme.util.network.BaseSendablePacket.ServerBasePacket#writeImpl()
	 */
	@Override
	protected void writeImpl()
	{
		writeC(0xfe);
		writeH(0x52);
	}
	
	/**
	 * @see ct25.xtreme.gameserver.BasePacket#getType()
	 */
	@Override
	public String getType()
	{
		return _S__FE_51_EXSHOWVARIATIONCANCELWINDOW;
	}
	
}
