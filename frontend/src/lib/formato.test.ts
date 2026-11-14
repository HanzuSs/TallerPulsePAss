import { expect, test } from 'vitest'
import { PrecioCOP } from './formato'
test('formatea precio en pesos colombianos', () => expect(PrecioCOP(250000)).toContain('250.000'))
