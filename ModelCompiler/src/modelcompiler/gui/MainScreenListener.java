/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package modelcompiler.gui;

/**
 *
 * @author VTPlusAKnauer
 */
public interface MainScreenListener {
	
	public void updateGUIData();
	public void importModel();
	public void openModel();
	public void loadMaterial();
	public void saveModel();
	public void playAnimation(String animation);
	public void centerExtents();
	public void copyExtents();
	public void findShelves();
	public void setShowShelves(boolean show);
}
