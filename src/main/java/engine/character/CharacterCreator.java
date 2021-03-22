package engine.character;

import static engine.EngineInputAction.DIALOG_MENU_ACTIONS;
import static engine.EngineInputAction.SELECT;
import static shared.ProgramMenuType.PROGRAM_SUB;

import java.util.function.Consumer;

import javax.annotation.Nonnull;

import io.vavr.collection.Array;
import io.vavr.collection.Seq;

import character.CharacterAlignment;
import character.CharacterGender;
import character.CharacterRace;
import character.ClassSelection;
import data.character.AbstractCharacter;
import engine.Engine;
import engine.EngineInputAction;
import engine.input.InputHandler;
import engine.rulesystem.Flavor;
import shared.CustomGoldboxString;
import shared.GoldboxString;
import shared.InputAction;
import shared.UserInterface;

public class CharacterCreator {

	private static final CustomGoldboxString PICK_RACE = new CustomGoldboxString("PICK A RACE");
	private static final CustomGoldboxString PICK_GENDER = new CustomGoldboxString("PICK A GENDER");
	private static final CustomGoldboxString PICK_CLASS = new CustomGoldboxString("PICK A CLASS");
	private static final CustomGoldboxString PICK_ALIGNMENT = new CustomGoldboxString("PICK ALIGNMENT");
	private static final CustomGoldboxString REROLL_STATS = new CustomGoldboxString("REROLL STATS:");

	private final InputHandler REROLL_HANDLER = new RerollStatsHandler();
	private final InputHandler NAME_HANDLER = new InputNameHandler();
	private final Seq<InputAction> CSHEET_ACTION = Array.of( //
		new EngineInputAction(REROLL_HANDLER, "YES"), //
		new EngineInputAction(NAME_HANDLER, "NO") //
	).map(InputAction.class::cast);

	private CharacterRace selectedRace;
	private CharacterGender selectedGender;
	private ClassSelection selectedClasses;
	private CharacterAlignment selectedAlignment;
	private CharacterSheetImpl cs;

	private final Flavor flavor;
	private final PlayerDataFactory playerDataFactory;
	private final UserInterface ui;
	private final Consumer<Runnable> taskHandler;

	public CharacterCreator(@Nonnull Flavor flavor, @Nonnull PlayerDataFactory playerDataFactory, @Nonnull UserInterface ui,
		@Nonnull Consumer<Runnable> taskHandler) {

		this.flavor = flavor;
		this.playerDataFactory = playerDataFactory;
		this.ui = ui;
		this.taskHandler = taskHandler;
	}

	public void start() {
		showPreogramSub(racesMenuItems(), PICK_RACE);
	}

	private Seq<InputAction> racesMenuItems() {
		final InputHandler handler = (a, e) -> setRaceAndSelectGender(e.getIndex());
		final Seq<CharacterRace> races = flavor.getRaces();
		return races.map(race -> raceToAction(race, handler, races));
	}

	private InputAction raceToAction(CharacterRace race, InputHandler handler, Seq<CharacterRace> races) {
		return new EngineInputAction(handler, race.getName(), races.indexOf(race));
	}

	private void setRaceAndSelectGender(int selectedRace) {
		this.selectedRace = flavor.getRaces().get(selectedRace);

		showPreogramSub(genderMenuItems(), PICK_GENDER);
	}

	private Seq<InputAction> genderMenuItems() {
		final InputHandler handler = (a, e) -> setGenderAndSelectClasses(e.getIndex());
		final Seq<CharacterGender> genders = Array.of(CharacterGender.values());
		return genders.map(gender -> genderToAction(gender, handler, genders));
	}

	private InputAction genderToAction(CharacterGender gender, InputHandler handler, Seq<CharacterGender> genders) {
		return new EngineInputAction(handler, gender.getName(), genders.indexOf(gender));
	}

	private void setGenderAndSelectClasses(int selectedGender) {
		this.selectedGender = CharacterGender.values()[selectedGender];

		showPreogramSub(classesMenuItems(flavor), PICK_CLASS);
	}

	private Seq<InputAction> classesMenuItems(Flavor flavor) {
		final InputHandler handler = (a, e) -> setClassesAndContinue(e.getIndex());
		final Seq<ClassSelection> classes = flavor.getClassSelections(selectedRace);
		return classes.map(sel -> classesToAction(sel, handler, classes));
	}

	private InputAction classesToAction(ClassSelection sel, InputHandler handler, Seq<ClassSelection> classes) {
		return new EngineInputAction(handler, sel.getDescription(), classes.indexOf(sel));
	}

	private void setClassesAndContinue(int selectedClasses) {
		this.selectedClasses = flavor.getClassSelections(selectedRace).get(selectedClasses);

		if (flavor.isAlignmentRequired()) {
			showPreogramSub(alignmentMenuItems(flavor), PICK_ALIGNMENT);
		} else {
			continueWithCharacter(playerDataFactory.createCharacter(this.selectedRace, this.selectedGender, this.selectedClasses));
		}
	}

	private Seq<InputAction> alignmentMenuItems(Flavor flavor) {
		final InputHandler handler = (a, e) -> setAlignmentAndContinue(e.getIndex());
		final Seq<CharacterAlignment> alignments = flavor.getAlignments(selectedClasses);
		return alignments.map(alignment -> alignmentToAction(alignment, handler, alignments));
	}

	private InputAction alignmentToAction(CharacterAlignment sel, InputHandler handler, Seq<CharacterAlignment> alignments) {
		return new EngineInputAction(handler, sel.getDescription(), alignments.indexOf(sel));
	}

	private void setAlignmentAndContinue(int selectedAlignment) {
		this.selectedAlignment = flavor.getAlignments(selectedClasses).get(selectedAlignment);

		continueWithCharacter(
			playerDataFactory.createCharacter(this.selectedRace, this.selectedGender, this.selectedClasses, this.selectedAlignment));
	}

	private void continueWithCharacter(AbstractCharacter pc) {
		flavor.initCharacter(pc, 0);
		cs = new CharacterSheetImpl(flavor, pc);
		ui.showCharacterSheet(cs, CSHEET_ACTION.asJava(), REROLL_STATS);
	}

	private void showPreogramSub(Seq<InputAction> input, GoldboxString heading) {
		taskHandler.accept(() -> {
			ui.showProgramMenuDialog(PROGRAM_SUB, input.asJava(), DIALOG_MENU_ACTIONS, heading, SELECT);
		});
	}

	private final class RerollStatsHandler implements InputHandler {
		@Override
		public void handle(Engine engine, EngineInputAction action) {
			engine.getUi().clearCurrentDialog();
			flavor.reroll(cs.getCharacter());
			engine.getUi().showCharacterSheet(cs, CSHEET_ACTION.asJava(), REROLL_STATS);
		}
	}

	private final class InputNameHandler implements InputHandler {
		@Override
		public void handle(Engine engine, EngineInputAction action) {
			engine.setNextTask(() -> {
				engine.setInputString(15);
				cs.getCharacter().setName(engine.getMemory().getInput().toString());

				engine.getMemory().addPartyMember(cs);

				engine.getUi().clearAllDialogs();
				engine.showProgramMenu();
			});
		}
	}

}