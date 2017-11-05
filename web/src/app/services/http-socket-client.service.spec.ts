import { TestBed, inject } from '@angular/core/testing';

import { HttpSocketClientService } from './http-socket-client.service';

describe('HttpSocketClientService', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [HttpSocketClientService]
    });
  });

  it('should be created', inject([HttpSocketClientService], (service: HttpSocketClientService) => {
    expect(service).toBeTruthy();
  }));
});
