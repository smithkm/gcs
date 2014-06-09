/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * version 2.0. If a copy of the MPL was not distributed with this file, You
 * can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * This Source Code Form is "Incompatible With Secondary Licenses", as defined
 * by the Mozilla Public License, version 2.0.
 */

package com.trollworks.gcs.menu.item;

import com.trollworks.gcs.advantage.Advantage;
import com.trollworks.gcs.advantage.AdvantagesDockable;
import com.trollworks.gcs.character.SheetDockable;
import com.trollworks.gcs.common.DataFile;
import com.trollworks.gcs.criteria.StringCompareType;
import com.trollworks.gcs.feature.Feature;
import com.trollworks.gcs.feature.SkillBonus;
import com.trollworks.gcs.modifier.Affects;
import com.trollworks.gcs.modifier.CostType;
import com.trollworks.gcs.modifier.Modifier;
import com.trollworks.gcs.skill.Skill;
import com.trollworks.gcs.skill.SkillOutline;
import com.trollworks.gcs.template.TemplateDockable;
import com.trollworks.gcs.widgets.outline.ListOutline;
import com.trollworks.toolkit.annotation.Localize;
import com.trollworks.toolkit.ui.widget.outline.OutlineModel;
import com.trollworks.toolkit.ui.widget.outline.OutlineProxy;
import com.trollworks.toolkit.utility.Localization;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * Provides the "Create Talent" command.
 *
 * @author Kevin Smith <smithkm@draconic.ca>
 **/
public class CreateTalentCommand extends com.trollworks.toolkit.ui.menu.Command {

	@Localize("Create Talent from Skills")
	private static String					CREATE_TALENT;

	@Localize("Create Talent")
	private static String					UNDO;

	@Localize("Talent (@Name@)")
	private static String					DEFAULT_NAME;

	@Localize("Alternative Cost")
	private static String					ALT_COST;

	@Localize("PU3:25")
	private static String					ALT_COST_REF;

	@Localize("Reaction Bonus")
	private static String					REACTION_BONUS;

	@Localize("@Reaction Bonus@")
	private static String					REACTION_BONUS_NOTES;

	@Localize("Alternate Benefit")
	private static String					ALT_BENEFIT;

	@Localize("@Alternate Benefit@")
	private static String					ALT_BENEFIT_NOTES;

	static {
		Localization.initialize();
	}

	/** The action command this command will issue. */
	public static final String				CMD_CREATE_TALENT	= "CreateTalent";						//$NON-NLS-1$
	private static final Collection<String>	CATEGORIES			= Arrays.asList("Talent", "Advantage"); //$NON-NLS-1$//$NON-NLS-2$

	/** The singleton {@link CreateTalentCommand}. */
	public static final CreateTalentCommand	INSTANCE			= new CreateTalentCommand();

	private CreateTalentCommand() {
		super(CREATE_TALENT, CMD_CREATE_TALENT);
	}

	@Override
	public void adjust() {
		Component focus = getFocusOwner();
		if (focus instanceof OutlineProxy) {
			focus = ((OutlineProxy) focus).getRealOutline();
		}

		// Enable if skills are selected.
		if (focus instanceof SkillOutline) {
			OutlineModel model = ((SkillOutline) focus).getModel();
			setEnabled(!model.isLocked() && model.hasSelection());
		} else {
			setEnabled(false);
		}
	}

	@SuppressWarnings("unchecked")
	// Should only ever occur to Skills
	static Collection<Skill> getSkills() {
		Component focus = getFocusOwner();
		if (focus instanceof OutlineProxy) {
			focus = ((OutlineProxy) focus).getRealOutline();
		}
		SkillOutline skillOutline = (SkillOutline) focus;
		OutlineModel skillModel = skillOutline.getModel();
		if (skillModel.hasSelection()) {
			@SuppressWarnings("rawtypes")
			List skillList = skillModel.getSelectionAsList(true);
			return skillList;
		}
		return null;
	}

	@Override
	public void actionPerformed(ActionEvent event) {
		Collection<Skill> skills = getSkills();
		ListOutline outline;
		DataFile dataFile;
		AdvantagesDockable adq = getTarget(AdvantagesDockable.class);
		if (adq != null) {
			dataFile = adq.getDataFile();
			outline = adq.getOutline();
			if (outline.getModel().isLocked()) {
				return;
			}
		} else {
			SheetDockable sheet = getTarget(SheetDockable.class);
			if (sheet != null) {
				dataFile = sheet.getDataFile();
				outline = sheet.getSheet().getAdvantageOutline();
			} else {
				TemplateDockable template = getTarget(TemplateDockable.class);
				if (template != null) {
					dataFile = template.getDataFile();
					outline = template.getTemplate().getAdvantageOutline();
				} else {
					return;
				}
			}
		}

		List<Feature> features = new ArrayList<>(skills.size());

		// give a per-level bonus to each skill
		for (Skill skill : skills) {
			SkillBonus bonus = new SkillBonus();
			bonus.getAmount().setPerLevel(true);
			bonus.getNameCriteria().setQualifier(skill.getName());
			bonus.getNameCriteria().setType(StringCompareType.IS);
			bonus.getSpecializationCriteria().setType(StringCompareType.IS_ANYTHING);
			features.add(bonus);
		}

		// Figure out the per-level cost
		final int standardPoints;
		final int smoothPoints = Math.max(5, skills.size());

		if (skills.size() <= 6) {
			standardPoints = 5;
		} else if (skills.size() <= 12) {
			standardPoints = 10;
		} else {
			standardPoints = 15;
		}

		// Make a modifier to change the cost to that resulting from
		// Smooth Talent Cost (Power-Ups 3, p. 25)
		Modifier smoothPointsModifier = new Modifier(dataFile);
		smoothPointsModifier.setName(ALT_COST);
		smoothPointsModifier.setCostType(CostType.POINTS);
		smoothPointsModifier.setAffects(Affects.LEVELS_ONLY);
		smoothPointsModifier.setCost(standardPoints - smoothPoints);
		smoothPointsModifier.setReference(ALT_COST_REF);
		smoothPointsModifier.setEnabled(false);

		// Make a modifier to describe the Reaction Bonus given by the talent
		Modifier reactionBonus = new Modifier(dataFile);
		reactionBonus.setName(REACTION_BONUS);
		reactionBonus.setNotes(REACTION_BONUS_NOTES);
		reactionBonus.setEnabled(true);

		// Make a modifier to describe the Alternate Benefit given by the talent
		Modifier alternateBenefit = new Modifier(dataFile);
		alternateBenefit.setName(ALT_BENEFIT);
		alternateBenefit.setNotes(ALT_BENEFIT_NOTES);
		alternateBenefit.setEnabled(false);

		// Assemble the talent
		Advantage advantage = new Advantage(dataFile, false);
		advantage.setFeatures(features);
		advantage.setCategories(CATEGORIES);
		advantage.setName(DEFAULT_NAME);
		advantage.setLevels(1);
		advantage.setPointsPerLevel(standardPoints);
		advantage.setModifiers(Arrays.asList(reactionBonus, alternateBenefit, smoothPointsModifier));

		// Add it to the sheet.
		// TODO Should do name substitution on the new advantage. KMS
		outline.addRow(advantage, UNDO, false);
		outline.getModel().select(advantage, false);
		outline.scrollSelectionIntoView();
		outline.openDetailEditor(true);
	}
}
