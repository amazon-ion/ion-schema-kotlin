package software.amazon.ionschema.util

import java.io.Closeable

/**
 * An Iterator that has the opportunity to free up any resources
 * upon [close()], after it is no longer needed.
 */
interface CloseableIterator<T> : Iterator<T>, Closeable

