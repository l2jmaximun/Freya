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
package ct25.xtreme.gameserver.network.gameserverpackets;

import java.io.IOException;

import ct25.xtreme.util.network.BaseSendablePacket;

/**
 * @author L0ngh0rn
 */
public class BlockAddress extends BaseSendablePacket
{
	public BlockAddress(String address, long expiration)
	{
		writeC(0x10);
		writeQ(expiration);
		writeS(address);
	}

	@Override
	public byte[] getContent() throws IOException
	{
		return getBytes();
	}
}
