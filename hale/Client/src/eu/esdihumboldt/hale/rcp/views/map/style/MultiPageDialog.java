/*
 * HUMBOLDT: A Framework for Data Harmonisation and Service Integration.
 * EU Integrated Project #030962                 01.10.2006 - 30.09.2010
 * 
 * For more information on the project, please refer to the this web site:
 * http://www.esdi-humboldt.eu
 * 
 * LICENSE: For information on the license under which this program is 
 * available, please refer to http:/www.esdi-humboldt.eu/license.html#core
 * (c) the HUMBOLDT Consortium, 2007 to 2010.
 */
package eu.esdihumboldt.hale.rcp.views.map.style;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.DialogTray;
import org.eclipse.jface.dialogs.IDialogPage;
import org.eclipse.jface.dialogs.IPageChangeProvider;
import org.eclipse.jface.dialogs.IPageChangedListener;
import org.eclipse.jface.dialogs.PageChangedEvent;
import org.eclipse.jface.dialogs.TrayDialog;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

/**
 * Dialog consisting of multiple {@link IDialogPage}s
 * @param <T> the dialog page type
 * 
 * @author Simon Templer
 * @partner 01 / Fraunhofer Institute for Computer Graphics Research
 * @version $Id$ 
 */
public abstract class MultiPageDialog<T extends IDialogPage> extends TrayDialog implements IPageChangeProvider {

	/**
	 * Dialog page item
	 */
	private class PageItem {
		
		private final T page;
		
		/**
		 * Creates a dialog page item
		 * 
		 * @param page the dialog page
		 */
		public PageItem(T page) {
			this.page = page;
		}
		
		/**
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			return page.getTitle();
		}

		/**
		 * @see java.lang.Object#hashCode()
		 */
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getOuterType().hashCode();
			result = prime * result + ((page == null) ? 0 : page.hashCode());
			return result;
		}

		/**
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			PageItem other = (PageItem) obj;
			if (!getOuterType().equals(other.getOuterType()))
				return false;
			if (page == null) {
				if (other.page != null)
					return false;
			} else if (!page.equals(other.page))
				return false;
			return true;
		}

		private MultiPageDialog<T> getOuterType() {
			return MultiPageDialog.this;
		}

	}

	/**
	 * Dialog tray
	 */
	private class PageTray extends DialogTray {
		
		private ListViewer viewer;
		
		/**
		 * @see DialogTray#createContents(Composite)
		 */
		@Override
		protected Control createContents(Composite parent) {
			viewer = new ListViewer(parent);
			
			viewer.setContentProvider(new IStructuredContentProvider() {
				
				@Override
				public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
					// TODO Auto-generated method stub
					
				}
				
				@Override
				public void dispose() {
					// ignore
				}
				
				@SuppressWarnings("unchecked")
				@Override
				public Object[] getElements(Object inputElement) {
					try {
						List<T> pages = (List<T>) inputElement;
						List<PageItem> values = new ArrayList<PageItem>();
						for (T page : pages) {
							values.add(new PageItem(page));
						}
						return values.toArray();
					} catch (Exception e) {
						return null;
					}
				}
			});
			
			viewer.setInput(pages);
			
			updateSelection();
			
			viewer.addSelectionChangedListener(new ISelectionChangedListener() {
				
				@Override
				public void selectionChanged(SelectionChangedEvent event) {
					if (event.getSelection() instanceof IStructuredSelection) {
						IStructuredSelection selection = (IStructuredSelection) event.getSelection();
						PageItem item = (PageItem) selection.getFirstElement();
						setCurrentPage(item.page);
					}
				}
			});
			
			return parent;
		}

		/**
		 * Update the selection in the viewer
		 */
		public void updateSelection() {
			if (viewer == null)
				return;
			
			T page = getCurrentPage();
			if (page != null) {
				viewer.setSelection(new StructuredSelection(new PageItem(page)));
			}
		}

	}

	private final Set<IPageChangedListener> pageListeners = new HashSet<IPageChangedListener>();
	
	private final List<T> pages = new ArrayList<T>();
	
	private int currentIndex = 0;
	
	private final PageTray tray = new PageTray();
	
	private String title;
	
	private Image image;
	
	/**
	 * Creates a new dialog using the current shell
	 */
	public MultiPageDialog() {
		this(Display.getCurrent().getActiveShell());
	}
	
	/**
	 * Creates a new dialog using the given shell
	 * 
	 * @param shell the shell
	 */
	public MultiPageDialog(Shell shell) {
		super(shell);
		
		setShellStyle(getShellStyle() | SWT.SHELL_TRIM);
	}

	/**
	 * @see Window#configureShell(Shell)
	 */
	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		
		newShell.setText(title);
		if (image != null) {
			newShell.setImage(image);
		}
	}

	/**
	 * @see Dialog#createContents(Composite)
	 */
	@Override
	protected Control createContents(Composite parent) {
		Control c = super.createContents(parent);
		
		createPages();
		
		updatePage();
		
		openTray(tray);
		
		return c;
	}

	/**
	 * Create the dialog pages and add them using the
	 * {@link #addPage(IDialogPage)} method
	 */
	protected abstract void createPages();
	
	/**
	 * Adds a dialog page
	 * 
	 * @param page the dialog page to add
	 */
	public void addPage(T page) {
		pages.add(page);
	}
	
	private void updatePage() {
		T page = getCurrentPage();
		
		if (page != null) {
			page.createControl((Composite) getDialogArea());
		}
	}

	/**
	 * @see IPageChangeProvider#addPageChangedListener(IPageChangedListener)
	 */
	@Override
	public void addPageChangedListener(IPageChangedListener listener) {
		pageListeners.add(listener);
	}

	/**
	 * @see IPageChangeProvider#getSelectedPage()
	 */
	@Override
	public Object getSelectedPage() {
		return getCurrentPage();
	}

	/**
	 * Get the current page
	 * 
	 * @return the current page
	 */
	public T getCurrentPage() {
		if (currentIndex < pages.size() && currentIndex >= 0) {
			return pages.get(currentIndex);
		}
		else {
			return null;
		}
	}
	
	private void setCurrentPage(T page) {
		T oldPage = getCurrentPage();
		
		if (allowPageChange(oldPage, page)) {
			int index = -1;
			int i = 0;
			
			Iterator<T> itPage = pages.iterator();
			while (index < 0 && itPage.hasNext()) {
				T p = itPage.next();
				
				if (p.equals(page)) {
					index = i;
				}
				
				i++;
			}
			
			if (index != currentIndex) {
				currentIndex = index;
				
				firePageChange(oldPage, page);
			}
		}
		else {
			tray.updateSelection();
			
			//TODO show some message
		}
	}

	/**
	 * @see IPageChangeProvider#removePageChangedListener(IPageChangedListener)
	 */
	@Override
	public void removePageChangedListener(IPageChangedListener listener) {
		pageListeners.remove(listener);
	}
	
	protected void firePageChange(T oldPage, T newPage) {
		onPageChange(oldPage, newPage);
		
		final PageChangedEvent pce = new PageChangedEvent(this, getSelectedPage());
		
		for (IPageChangedListener listener : pageListeners) {
			listener.pageChanged(pce);
		}
	}

	/**
	 * Called after the page has changed
	 * 
	 * @param oldPage the old page
	 * @param newPage the new page
	 */
	protected abstract void onPageChange(T oldPage, T newPage);
	
	/**
	 * Called before the page changes
	 * 
	 * @param oldPage the old page
	 * @param newPage the new page
	 * @return if the page change is allowed
	 */
	protected abstract boolean allowPageChange(T oldPage, T newPage);
	
	/**
	 * @return the title
	 */
	public String getTitle() {
		return title;
	}

	/**
	 * @param title the title to set
	 */
	public void setTitle(String title) {
		this.title = title;
	}

	/**
	 * @return the image
	 */
	public Image getImage() {
		return image;
	}

	/**
	 * @param image the image to set
	 */
	public void setImage(Image image) {
		this.image = image;
	}

}
