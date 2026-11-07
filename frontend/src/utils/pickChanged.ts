/**
 * Builds a partial object with only keys whose values differ from the original snapshot.
 */
export function pickChanged<T extends Record<string, unknown>>(
  original: T,
  current: T,
  keys: readonly (keyof T)[],
): Partial<T> {
  const result: Partial<T> = {}
  for (const key of keys) {
    if (current[key] !== original[key]) {
      result[key] = current[key]
    }
  }
  return result
}

export function hasChanges<T extends Record<string, unknown>>(partial: Partial<T>): boolean {
  return Object.keys(partial).length > 0
}
