// ******************************************************* 
//                   MACHINE GENERATED CODE                
//                       DO NOT MODIFY                     
//                                                         
// Generated on 09/22/2015 21:15:27
// ******************************************************* 
package crawler.characters.ai.jbt;

/**
 * BT library that includes the trees read from the following files:
 * <ul>
 * <li>assets\JBT\roam.xbt</li>
 * </ul>
 */
public class BTLibrary implements jbt.execution.core.IBTLibrary {
	/** Tree generated from file assets\JBT\roam.xbt. */
	private static jbt.model.core.ModelTask Roam;

	/* Static initialization of all the trees. */
	static {
		Roam = new jbt.model.task.decorator.ModelRepeat(
				null,
				new jbt.model.task.composite.ModelSequence(
						null,
						new jbt.model.task.decorator.ModelInterrupter(
								null,
								new jbt.model.task.composite.ModelParallel(
										null,
										jbt.model.task.composite.ModelParallel.ParallelPolicy.SELECTOR_POLICY,
										new jbt.model.task.decorator.ModelRepeat(
												null,
												new jbt.model.task.composite.ModelSequence(
														null,
														new crawler.characters.ai.jbt.actions.model.ComputeRandomClosePosition(
																null, null,
																"entityPosition"),
														new crawler.characters.ai.jbt.actions.model.Move(
																null, null,
																"closePosition"),
														new jbt.model.task.leaf.ModelWait(
																null, 1000))),
										new jbt.model.task.decorator.ModelRepeat(
												null,
												new jbt.model.task.composite.ModelSequence(
														null,
														new jbt.model.task.leaf.ModelWait(
																null, 200),
														new crawler.characters.ai.jbt.conditions.model.CanSee(
																null, "player",
																null),
														new jbt.model.task.leaf.ModelPerformInterruption(
																null,
																null,
																jbt.execution.core.ExecutionTask.Status.SUCCESS))))),
						new jbt.model.task.decorator.ModelRepeat(
								null,
								new jbt.model.task.composite.ModelSequence(
										null,
										new jbt.model.task.composite.ModelSequence(
												null,
												new crawler.characters.ai.jbt.actions.model.Advance(
														null, "player", null),
												new crawler.characters.ai.jbt.actions.model.Attack(
														null, "player", null)),
										new jbt.model.task.composite.ModelSequence(
												null,
												new crawler.characters.ai.jbt.actions.model.Follow(
														null, "player", null))))));

		((jbt.model.task.leaf.ModelPerformInterruption) Roam
				.findNode(new jbt.model.core.ModelTask.Position(0, 0, 0, 1, 0,
						2)))
				.setInterrupter((jbt.model.task.decorator.ModelInterrupter) Roam
						.findNode(new jbt.model.core.ModelTask.Position(0, 0)));

	}

	/**
	 * Returns a behaviour tree by its name, or null in case it cannot be found.
	 * It must be noted that the trees that are retrieved belong to the class,
	 * not to the instance (that is, the trees are static members of the class),
	 * so they are shared among all the instances of this class.
	 */
	public jbt.model.core.ModelTask getBT(java.lang.String name) {
		if (name.equals("Roam")) {
			return Roam;
		}
		return null;
	}

	/**
	 * Returns an Iterator that is able to iterate through all the elements in
	 * the library. It must be noted that the iterator does not support the
	 * "remove()" operation. It must be noted that the trees that are retrieved
	 * belong to the class, not to the instance (that is, the trees are static
	 * members of the class), so they are shared among all the instances of this
	 * class.
	 */
	public java.util.Iterator<jbt.util.Pair<java.lang.String, jbt.model.core.ModelTask>> iterator() {
		return new BTLibraryIterator();
	}

	private class BTLibraryIterator
			implements
			java.util.Iterator<jbt.util.Pair<java.lang.String, jbt.model.core.ModelTask>> {
		static final long numTrees = 1;
		long currentTree = 0;

		public boolean hasNext() {
			return this.currentTree < numTrees;
		}

		public jbt.util.Pair<java.lang.String, jbt.model.core.ModelTask> next() {
			this.currentTree++;

			if ((this.currentTree - 1) == 0) {
				return new jbt.util.Pair<java.lang.String, jbt.model.core.ModelTask>(
						"Roam", Roam);
			}

			throw new java.util.NoSuchElementException();
		}

		public void remove() {
			throw new java.lang.UnsupportedOperationException();
		}
	}
}
