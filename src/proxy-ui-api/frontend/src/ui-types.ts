/*
 TypeScript typings that are used in UI, but not in backend.
 These are not in openapi definitions.
*/
import { Client } from '@/openapi-types';

// Interface for Tab data
export interface Tab {
  key: string;
  name: string;
  to: {
    name: string;
    params?: {
      id?: string;
    };
  };
  permissions?: string[];
}

// Extension for Client
export type ExtendedClient = Client & {
  visibleName: string;
  isFiltered?: boolean;
  type: string;
  id: string;
};

// Used in service clients views for listing services than can be granted access rights to
export interface ServiceCandidate {
  service_code: string;
  service_title?: string;
  id: string;
}
