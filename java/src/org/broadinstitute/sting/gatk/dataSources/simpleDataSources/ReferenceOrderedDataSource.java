package org.broadinstitute.sting.gatk.dataSources.simpleDataSources;

import org.broadinstitute.sting.gatk.refdata.ReferenceOrderedDatum;
import org.broadinstitute.sting.gatk.refdata.ReferenceOrderedData;
import org.broadinstitute.sting.gatk.refdata.RODIterator;
import org.broadinstitute.sting.gatk.dataSources.shards.Shard;
import org.broadinstitute.sting.utils.GenomeLoc;

import java.util.Iterator;
import java.util.List;
/**
 * User: hanna
 * Date: May 21, 2009
 * Time: 10:04:12 AM
 * BROAD INSTITUTE SOFTWARE COPYRIGHT NOTICE AND AGREEMENT
 * Software and documentation are copyright 2005 by the Broad Institute.
 * All rights are reserved.
 *
 * Users acknowledge that this software is supplied without any warranty or support.
 * The Broad Institute is not responsible for its use, misuse, or
 * functionality.
 */

/**
 * A data source which provides a single type of reference-ordered data.
 */
public class ReferenceOrderedDataSource implements SimpleDataSource {
    /**
     * The reference-ordered data itself.
     */
    private final ReferenceOrderedData rod;

    /**
     * A pool of iterators for navigating through the genome.
     */
    private ReferenceOrderedDataPool iteratorPool = null;

    /**
     * Create a new reference-ordered data source.
     * @param rod
     */
    public ReferenceOrderedDataSource( ReferenceOrderedData rod) {
        this.rod = rod;
        this.iteratorPool = new ReferenceOrderedDataPool( rod );
    }

    /**
     * Return the name of the underlying reference-ordered data.
     * @return Name of the underlying rod.
     */
    public String getName() {
        return this.rod.getName();
    }

    /**
     * Seek to the specified position and return an iterator through the data.
     * @param shard Shard that points to the selected position.
     * @return Iterator through the data.
     */
    public Iterator seek( Shard shard ) {
        RODIterator iterator = iteratorPool.iterator(shard.getGenomeLoc());
        return iterator;
    }

    /**
     * Close the specified iterator, returning it to the pool.
     * @param iterator Iterator to close.
     */
    public void close( RODIterator iterator ) {
        this.iteratorPool.release(iterator);        
    }

}

/**
 * A pool of reference-ordered data iterators.
 */
class ReferenceOrderedDataPool extends ResourcePool<RODIterator,RODIterator> {
    private final ReferenceOrderedData<? extends ReferenceOrderedDatum> rod;

    public ReferenceOrderedDataPool( ReferenceOrderedData<? extends ReferenceOrderedDatum> rod ) {
        this.rod = rod;
    }

    /**
     * Create a new iterator from the existing reference-ordered data.  This new iterator is expected
     * to be completely independent of any other iterator.
     * @param position @{inheritedDoc}
     * @return The newly created resource.
     */
    public RODIterator createNewResource( GenomeLoc position ) {
        return rod.iterator();
    }

    /**
     * Finds the best existing ROD iterator from the pool.  In this case, the best existing ROD is defined as
     * the first one encountered that is at or before the given position.
     * @param position @{inheritedDoc}
     * @param resources @{inheritedDoc}
     * @return @{inheritedDoc}
     */
    public RODIterator selectBestExistingResource( GenomeLoc position, List<RODIterator> resources ) {
        for( RODIterator iterator: resources ) {
            if( (iterator.position() == null && iterator.hasNext()) ||
                (iterator.position() != null && iterator.position().isBefore(position)) )
                return iterator;
        }
        return null;
    }

    /**
     * In this case, the iterator is the resource.  Pass it through.
     */
    public RODIterator createIteratorFromResource( GenomeLoc position, RODIterator resource ) {
        return resource;
    }

    /**
     * Don't worry about closing the resource; let the file handles expire naturally for the moment.
     */
    public void closeResource(  RODIterator resource ) {
        
    }
}


