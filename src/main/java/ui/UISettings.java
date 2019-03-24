package ui;

import static ui.ScaleMethod.NONE;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashSet;
import java.util.Set;

import javax.annotation.Nonnull;

public class UISettings {
	private int zoom;
	private ScaleMethod method;

	private Set<PropertyChangeListener> listeners = new HashSet<>();

	public UISettings() {
		zoom = 4;
		method = NONE;
	}

	@Nonnull
	public ScaleMethod getMethod() {
		return method;
	}

	public void setMethod(@Nonnull ScaleMethod method) {
		ScaleMethod old = getMethod();
		this.method = method;
		firePropertyChange(PropertyName.METHOD, old, method);
	}

	public int getZoom() {
		return zoom;
	}

	public void setZoom(int zoom) {
		int old = getZoom();
		this.zoom = zoom;
		firePropertyChange(PropertyName.ZOOM, old, zoom);
	}

	public int zoom(int value) {
		return getZoom() * value;
	}

	public int zoom8(int value) {
		return getZoom() * 8 * value;
	}

	public void addPropertyChangeListener(@Nonnull PropertyChangeListener l) {
		listeners.add(l);
	}

	public void removePropertyChangeListener(@Nonnull PropertyChangeListener l) {
		listeners.remove(l);
	}

	private void firePropertyChange(@Nonnull PropertyName n, Object oldValue, Object newValue) {
		PropertyChangeEvent e = new PropertyChangeEvent(this, n.name(), oldValue, newValue);
		for (PropertyChangeListener l : listeners) {
			l.propertyChange(e);
		}
	}

	public enum PropertyName {
		ZOOM, METHOD;
	}
}
