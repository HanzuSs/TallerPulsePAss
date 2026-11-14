export const PrecioCOP = (valor: number) => new Intl.NumberFormat('es-CO', { style: 'currency', currency: 'COP', maximumFractionDigits: 0 }).format(valor)
export const FechaCorta = (valor: string) => new Intl.DateTimeFormat('es-CO', { dateStyle: 'medium' }).format(new Date(valor))
export const FechaHora = (valor: string) => new Intl.DateTimeFormat('es-CO', { dateStyle: 'medium', timeStyle: 'short' }).format(new Date(valor))
