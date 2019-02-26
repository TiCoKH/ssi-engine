package ui;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.collect.ImmutableList;

import engine.InputAction;
import types.GoldboxString;
import types.MenuType;

public class Menu {

	private MenuType type;
	private List<InputAction> menu;
	private List<GoldboxString> menuItems;

	private Optional<GoldboxString> description = Optional.empty();

	private int selection = 0;

	public Menu(@Nonnull MenuType type, @Nonnull List<InputAction> menu, @Nullable GoldboxString description) {
		this.type = type;
		this.menu = ImmutableList.copyOf(menu);
		this.menuItems = menu.stream().filter(a -> a.getName().isPresent()).map(a -> a.getName().get()).collect(Collectors.toList());
		this.description = Optional.ofNullable(description);
	}

	public Optional<GoldboxString> getDescription() {
		return description;
	}

	public int getItemCount() {
		return menuItems.size();
	}

	public GoldboxString getMenuItem(int index) {
		return menuItems.get(index);
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
		if (selection + 1 < menuItems.size())
			selection++;
		else
			selection = 0;
	}

	public void prev() {
		if (selection - 1 >= 0)
			selection--;
		else
			selection = menuItems.size() - 1;
	}
}
