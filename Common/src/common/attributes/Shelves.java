/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package common.attributes;

import com.jme3.bounding.BoundingBox;
import com.jme3.scene.attribute.AbstractAttribute;
import java.util.ArrayList;
import lombok.Getter;

/**
 *
 * @author VTPlusAKnauer
 */
public class Shelves extends AbstractAttribute {

	@Getter private final ArrayList<BoundingBox> shelves;

	public Shelves(ArrayList<BoundingBox> shelves) {
		this.shelves = shelves;
	}
}
