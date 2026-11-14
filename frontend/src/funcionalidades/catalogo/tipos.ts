import { z } from 'zod'

export const EventCategorySchema = z.enum(['MUSIC', 'SPORTS', 'TECHNOLOGY', 'EDUCATION', 'CULTURE', 'ENTERTAINMENT'])
export const EventStatusSchema = z.enum(['DRAFT', 'PUBLISHED', 'SOLD_OUT', 'CANCELLED', 'FINISHED'])
export const TicketTypeSchema = z.enum(['GENERAL', 'VIP', 'BACKSTAGE', 'STUDENT'])
export const TicketStatusSchema = z.enum(['RESERVED', 'PAID', 'CANCELLED', 'USED'])
export const RolSchema = z.enum(['USER', 'STAFF', 'ADMIN'])

export const ArtistSchema = z.object({ id: z.number(), stageName: z.string(), country: z.string().nullable().optional(), genre: z.string().nullable().optional(), active: z.boolean() })
export const VenueSchema = z.object({ id: z.number(), code: z.string(), name: z.string(), city: z.string(), address: z.string().nullable().optional(), capacity: z.number(), active: z.boolean() })
export const EventSummarySchema = z.object({ id: z.number(), eventCode: z.string(), name: z.string(), category: EventCategorySchema, status: EventStatusSchema, eventDate: z.string(), venueCode: z.string(), venueName: z.string() })
export const EventSchema = EventSummarySchema.extend({ description: z.string().nullable().optional(), minimumAge: z.number(), artists: z.array(ArtistSchema) })
export const TicketSchema = z.object({ id: z.number(), ticketCode: z.string(), type: TicketTypeSchema, price: z.coerce.number(), status: TicketStatusSchema, purchaseDate: z.string(), userEmail: z.string().email(), eventCode: z.string(), eventName: z.string() })
export const UserSchema = z.object({ id: z.number(), username: z.string(), email: z.string().email(), active: z.boolean(), firstName: z.string(), lastName: z.string(), phone: z.string().nullable().optional(), city: z.string().nullable().optional(), birthDate: z.string() })
export const AuthResponseSchema = z.object({ accessToken: z.string().min(1), tokenType: z.string(), expiresIn: z.number().positive() })
export const AuthenticatedUserSchema = z.object({ id: z.number(), username: z.string(), email: z.string().email(), roles: z.array(RolSchema).or(z.set(RolSchema)).transform((roles) => Array.from(roles)) })
export const ErrorResponseSchema = z.object({ timestamp: z.string().optional(), status: z.number(), error: z.string().optional(), message: z.string().min(1), details: z.record(z.string(), z.string()).optional() })

export type EventCategory = z.infer<typeof EventCategorySchema>
export type EventStatus = z.infer<typeof EventStatusSchema>
export type TicketType = z.infer<typeof TicketTypeSchema>
export type TicketStatus = z.infer<typeof TicketStatusSchema>
export type Rol = z.infer<typeof RolSchema>
export type Artist = z.infer<typeof ArtistSchema>
export type Venue = z.infer<typeof VenueSchema>
export type EventSummary = z.infer<typeof EventSummarySchema>
export type Event = z.infer<typeof EventSchema>
export type Ticket = z.infer<typeof TicketSchema>
export type User = z.infer<typeof UserSchema>
export type AuthenticatedUser = z.infer<typeof AuthenticatedUserSchema>
