package transformations;

import ct25.xtreme.gameserver.datatables.SkillTable;
import ct25.xtreme.gameserver.instancemanager.TransformationManager;
import ct25.xtreme.gameserver.model.L2Transformation;

public class ArcherCaptain extends L2Transformation
{
	private static final int[] SKILLS = new int[]
	{
		5491,
		619
	};

	public ArcherCaptain()
	{
		// id, colRadius, colHeight
		super(17, 10, 23.5);
	}

	@Override
	public void onTransform()
	{
		if (getPlayer().getTransformationId() != 17 || getPlayer().isCursedWeaponEquipped())
			return;

		transformedSkills();
	}

	public void transformedSkills()
	{
		// Decrease Bow/Crossbow Attack Speed
		getPlayer().addSkill(SkillTable.getInstance().getInfo(5491, 1), false);
		// Transform Dispel
		getPlayer().addSkill(SkillTable.getInstance().getInfo(619, 1), false);

		getPlayer().setTransformAllowedSkills(SKILLS);
	}

	@Override
	public void onUntransform()
	{
		removeSkills();
	}

	public void removeSkills()
	{
		// Decrease Bow/Crossbow Attack Speed
		getPlayer().removeSkill(SkillTable.getInstance().getInfo(5491, 1), false);
		// Transform Dispel
		getPlayer().removeSkill(SkillTable.getInstance().getInfo(619, 1), false);

		getPlayer().setTransformAllowedSkills(EMPTY_ARRAY);
	}

	public static void main(final String[] args)
	{
		TransformationManager.getInstance().registerTransformation(new ArcherCaptain());
	}
}