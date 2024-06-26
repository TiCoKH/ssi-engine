package ui.shared;

import java.util.Optional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import io.vavr.collection.Seq;

import shared.GoldboxString;
import shared.InputAction;
import shared.MenuType;

public class Menu {

	private MenuType type;
	private Seq<InputAction> menu;

	private Optional<GoldboxString> description = Optional.empty();

	private int selection = 0;

	public Menu(@Nonnull MenuType type, @Nonnull Seq<InputAction> menu) {
		this(type, menu, null);
	}

	public Menu(@Nonnull MenuType type, @Nonnull Seq<InputAction> menu, @Nullable GoldboxString description) {
		this.type = type;
		this.menu = menu;
		this.description = Optional.ofNullable(description);
	}

	public Optional<GoldboxString> getDescription() {
		return description;
	}

	public int getItemCount() {
		return menu.size();
	}

	public GoldboxString getMenuItem(int index) {
		return menu.get(index).getName();
	}

	public int getSelectedIndex() {
		return selection;
	}

	public InputAction getSelectedItem() {
		return menu.get(selection);
	}

	public boolean isSelected(int index) {
		return index == selection;
	}

	public MenuType getType() {
		return type;
	}

	public void next() {
		if (selection + 1 < menu.size())
			selection++;
		else
			selection = 0;
	}

	public void prev() {
		if (selection - 1 >= 0)
			selection--;
		else
			selection = menu.size() - 1;
	}

	public void setSelectedItem(InputAction selected) {
		selection = menu.indexOf(selected);
	}
}
