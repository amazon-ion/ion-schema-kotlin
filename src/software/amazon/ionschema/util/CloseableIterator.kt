package software.amazon.ionschema.util

import java.io.Closeable

interface CloseableIterator<T> : Iterator<T>, Closeable

