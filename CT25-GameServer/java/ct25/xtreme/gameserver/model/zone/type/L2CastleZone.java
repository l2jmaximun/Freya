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
package ct25.xtreme.gameserver.model.zone.type;

import ct25.xtreme.gameserver.ThreadPoolManager;
import ct25.xtreme.gameserver.model.actor.L2Character;
import ct25.xtreme.gameserver.model.actor.instance.L2PcInstance;
import ct25.xtreme.gameserver.model.zone.L2SpawnZone;
import ct25.xtreme.util.Rnd;


/**
 * An Custom PvP Zone
 *
 * @author  NeverMore
 */
public class L2CustomPvP extends L2SpawnZone
{
    //Ramdom Locations configs
    private static int[] _x = {11551, 10999, 10401};
    private static int[] _y = {-24264, -23576, -24030};
    private static int[] _z = {-3644, -3651, -3660 };
    
    public L2CustomPvP(int id)
    {
        super(5555);
    }
    
    @Override
    protected void onEnter(L2Character character)
    {
        character.setInsideZone(L2Character.ZONE_PVP, true);
        character.setInsideZone(L2Character.ZONE_NOSUMMONFRIEND, true);
        
           if (character instanceof L2PcInstance)
           {
               ((L2PcInstance) character).sendMessage("You enter a PvP Area");
               ((L2PcInstance) character).setPvpFlag(1);               
           }
    }
    
    @Override
    protected void onExit(L2Character character)
    {
        character.setInsideZone(L2Character.ZONE_PVP, false);
        character.setInsideZone(L2Character.ZONE_NOSUMMONFRIEND, false);
        
        if (character instanceof L2PcInstance)
        {
            ((L2PcInstance) character).stopNoblesseBlessing(null);
            ((L2PcInstance) character).setPvpFlag(0);
            ((L2PcInstance) character).sendMessage("You exit from a PvP Area");
        }
    }
    
    static class BackToPvp implements Runnable
    {
        private L2Character _activeChar;

        BackToPvp(L2Character character)
        {
            _activeChar = character;
        }

        @Override
        public void run()
        {
            int r = Rnd.get(3);
            _activeChar.teleToLocation(_x[r] , _y[r], _z[r]);
        }
    }
    
    @Override
    public void onDieInside(L2Character character)
    {
           if (character instanceof L2PcInstance)
           {
           }
    }
    
    @Override
    public void onReviveInside(L2Character character)
    {
        ThreadPoolManager.getInstance().scheduleGeneral(new BackToPvp(character), 500);
        ((L2PcInstance) character).isNoblesseBlessed();
    }
}