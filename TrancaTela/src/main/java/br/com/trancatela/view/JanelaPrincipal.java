package br.com.trancatela.view;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.swing.AbstractListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListModel;

import br.com.trancatela.util.BluetoothUtil;

class SortedListModel extends AbstractListModel {
	SortedSet<Object> model;

	public SortedListModel() {
		model = new TreeSet<Object>();
	}

	public int getSize() {
		return model.size();
	}

	public Object getElementAt(int index) {
		return model.toArray()[index];
	}

	public void add(Object element) {
		if (model.add(element)) {
			fireContentsChanged(this, 0, getSize());
		}
	}

	public void addAll(Object elements[]) {
		Collection<Object> c = Arrays.asList(elements);
		model.addAll(c);
		fireContentsChanged(this, 0, getSize());
	}

	public void clear() {
		model.clear();
		fireContentsChanged(this, 0, getSize());
	}

	public boolean contains(Object element) {
		return model.contains(element);
	}

	public Object firstElement() {
		return model.first();
	}

	public Iterator iterator() {
		return model.iterator();
	}

	public Object lastElement() {
		return model.last();
	}

	public boolean removeElement(Object element) {
		boolean removed = model.remove(element);
		if (removed) {
			fireContentsChanged(this, 0, getSize());
		}
		return removed;
	}
}

public class JanelaPrincipal extends JPanel {
	private JList sourceList;

	private SortedListModel sourceListModel;

	private JList destList;

	private SortedListModel destListModel;

	private JButton getDevicesButton;

	private JButton addButton;

	private JButton removeButton;

	private JButton executeButton;

	private JLabel dbmLabel;

	private JLabel dbmUnidade;

	private JTextField dbmTextField;
	
	BluetoothUtil bluetoothUtil;

	public JanelaPrincipal() {
		initScreen();
	}

	public void clearSourceListModel() {
		sourceListModel.clear();
	}

	public void clearDestinationListModel() {
		destListModel.clear();
	}

	public void addSourceElements(ListModel newValue) {
		fillListModel(sourceListModel, newValue);
	}

	public void setSourceElements(ListModel newValue) {
		clearSourceListModel();
		addSourceElements(newValue);
	}

	public void addDestinationElements(ListModel newValue) {
		fillListModel(destListModel, newValue);
	}

	private void fillListModel(SortedListModel model, ListModel newValues) {
		int size = newValues.getSize();
		for (int i = 0; i < size; i++) {
			model.add(newValues.getElementAt(i));
		}

	}

	public void addSourceElements(Object newValue[]) {
		fillListModel(sourceListModel, newValue);
	}

	public void setSourceElements(Object newValue[]) {
		clearSourceListModel();
		addSourceElements(newValue);
	}

	public void addDestinationElements(Object newValue[]) {
		fillListModel(destListModel, newValue);
	}

	private void fillListModel(SortedListModel model, Object newValues[]) {
		model.addAll(newValues);
	}

	private void clearSourceSelected() {
		Object selected[] = sourceList.getSelectedValues();
		for (int i = selected.length - 1; i >= 0; --i) {
			sourceListModel.removeElement(selected[i]);
		}
		sourceList.getSelectionModel().clearSelection();
	}
	
	private void clearSource() {
		
		for (int i = sourceList.getModel().getSize() - 1; i >= 0; --i) {
			sourceListModel.removeElement(sourceList.getModel().getElementAt(i));
		}
		sourceList.getSelectionModel().clearSelection();

	}
	
	private void clearDestination() {
		
		for (int i = destList.getModel().getSize() - 1; i >= 0; --i) {
			destListModel.removeElement(destList.getModel().getElementAt(i));
		}
		destList.getSelectionModel().clearSelection();

	}

	private void clearDestinationSelected() {
		Object selected[] = destList.getSelectedValues();
		for (int i = selected.length - 1; i >= 0; --i) {
			destListModel.removeElement(selected[i]);
		}
		destList.getSelectionModel().clearSelection();
	}

	private void initScreen() {
		setLayout(new GridLayout(0, 3));
		sourceListModel = new SortedListModel();
		sourceList = new JList(sourceListModel);

		getDevicesButton = new JButton("Atualizar");
		getDevicesButton.addActionListener(new GetDevicesListener());

		addButton = new JButton(">>");
		addButton.addActionListener(new AddListener());
		removeButton = new JButton("<<");
		removeButton.addActionListener(new RemoveListener());

		dbmLabel = new JLabel(
				"<html>Bloquear com valor<br/> de sinal abaixo de:</html>");
		dbmTextField = new JTextField("-5", 4);
		dbmTextField.setSize(30, 10);
		dbmUnidade = new JLabel("dbm");

		executeButton = new JButton("Executar");
		executeButton.addActionListener(new ExecuteListener());

		destListModel = new SortedListModel();
		destList = new JList(destListModel);

		JPanel leftPanel = new JPanel(new BorderLayout());
		leftPanel.add(new JLabel("Dispon’veis:"), BorderLayout.NORTH);
		leftPanel.add(new JScrollPane(sourceList), BorderLayout.CENTER);

		JPanel centerPanel = new JPanel(new FlowLayout());
		centerPanel.add(getDevicesButton);
		centerPanel.add(addButton);
		centerPanel.add(removeButton);
		centerPanel.add(dbmLabel);
		centerPanel.add(dbmTextField);
		centerPanel.add(dbmUnidade);
		centerPanel.add(executeButton);

		JPanel rightPanel = new JPanel(new BorderLayout());

		rightPanel.add(new JLabel("Selecionados:"), BorderLayout.NORTH);
		rightPanel.add(new JScrollPane(destList), BorderLayout.CENTER);

		add(leftPanel);
		add(centerPanel);
		add(rightPanel);
	}

	private class GetDevicesListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			clearSource();
			clearDestination();
			bluetoothUtil = new BluetoothUtil();
			String[] nomes = bluetoothUtil.getDevices();
			addSourceElements(nomes);
		}
	}

	private class AddListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			Object selected[] = sourceList.getSelectedValues();
			addDestinationElements(selected);
			clearSourceSelected();
		}
	}

	private class ExecuteListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			List<String> nomes = new ArrayList<String>();
			for (int i = 0; i < destList.getModel().getSize(); i++) {
				nomes.add(destList.getModel().getElementAt(i).toString());
			}
			int rssi = Integer.parseInt(dbmTextField.getText());
			if (nomes.size() > 0){
				bluetoothUtil.execute(nomes, rssi);
			}
		}
	}

	private class RemoveListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			Object selected[] = destList.getSelectedValues();
			addSourceElements(selected);
			clearDestinationSelected();
		}
	}

	public static void main(String args[]) {
		JFrame frame = new JFrame("Tranca Tela");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		JanelaPrincipal dual = new JanelaPrincipal();
		frame.add(dual, BorderLayout.CENTER);
		frame.setSize(400, 300);
		frame.setVisible(true);
	}
}