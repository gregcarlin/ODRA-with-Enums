package odra.cli.gui.components.storage;

import odra.util.Worker;

/**
 * Heap structure downloader class.
 *
 * @author jacenty
 * @version 2007-01-04
 * @since 2007-01-04
 */
class Downloader
{
	/** worker */
	private Worker worker;
	/** heap panel */
	final HeapPanel heapPanel;
	
	/**
	 * The construvtor.
	 * 
	 * @param heapPanel heap panel
	 */
	Downloader(HeapPanel heapPanel)
	{
		this.heapPanel = heapPanel;
	}
	
	void go()
  {
    worker = new Worker()
    {
      @Override
			public Object construct()
      {
        return new ActualTask();
      }
    };
    worker.start();
  }
	
	class ActualTask
  {
    ActualTask()
    {
    	heapPanel.getNextFragment();
    }
  }
}
