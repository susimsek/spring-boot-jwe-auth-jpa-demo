export const DEFAULT_AVATAR = '/images/default-avatar.png';
// utils/app.js

/**
 * Sends a fetch request, handles redirects and 401 globally.
 */
export async function apiRequest(url, opts = {}) {
  // Centralized response handling (handles 401, 403, 429, 500, 503)
  async function handleResponse(status, data) {
    const isAuthEndpoint = url.endsWith('/api/v1/auth/login')
      || url.endsWith('/api/v1/auth/refresh-token');
    if (status === 401 && !isAuthEndpoint && !opts._retry) {
      if (!data || data.error === 'invalid_token') {
        opts._retry = true;
        const r = await fetch('/api/v1/auth/refresh-token', {
          method: 'POST',
          credentials: 'include'
        });
        if (r.ok) {
          return apiRequest(url, opts);
        } else {
          localStorage.removeItem('profile');
          window.location.href = '/login';
          return;
        }
      }
      return { status, data };
    }
    if (status === 403) {
      if (!data || data.error === 'access_denied') {
        window.location.href = '/403';
        return;
      }
      return { status, data };
    }
    if (status === 429) {
      const retryAfter = parseInt(r.headers?.get('Retry-After') ?? '0', 10);
      data.detail = `Too many requests. Please wait ${retryAfter} seconds before retrying.`;
      return { status, data };
    }
    if (status === 500) {
      data.detail = "Sorry, something went wrong on our end. Please try again later.";
      return { status, data };
    }
    if (status === 503) {
      data.detail = "Service is temporarily unavailable. Please wait a few minutes and try again.";
      return { status, data };
    }
    return { status, data };
  }

  // If onUploadProgress is provided, use XHR to track upload progress
  if (typeof opts.onUploadProgress === 'function') {
    return new Promise((resolve, reject) => {
      const xhr = new XMLHttpRequest();
      xhr.open(opts.method || 'GET', url);
      // set headers
      Object.entries(opts.headers || {}).forEach(([k, v]) => xhr.setRequestHeader(k, v));
      xhr.withCredentials = opts.credentials === 'include';

      // upload progress handler
      xhr.upload.onprogress = opts.onUploadProgress;

      xhr.onload = () => {
        const status = xhr.status;
        const contentType = xhr.getResponseHeader('Content-Type') || '';
        let data;
        try {
          if (contentType.includes('application/json') || contentType.includes('application/problem+json')) {
            data = JSON.parse(xhr.responseText);
          } else {
            data = xhr.responseText;
          }
        } catch (_) {
          data = null;
        }
        // Delegate to centralized handler
        handleResponse(status, data).then(resolve).catch(reject);
      };
      xhr.onerror = () => reject(new Error('Network error'));
      xhr.send(opts.body);
    });
  }

  // Default headers + include credentials for cookies
  opts.headers = {
    ...(opts.headers || {}),
    Accept: opts.headers?.Accept ?? 'application/json',
    'Accept-Language': 'en'
  };
  opts.credentials = 'include';
  opts.redirect    = 'manual';

  console.log(`apiRequest called with URL: ${url}`, opts);

  try {
    let res = await fetch(url, opts);

    if (res.redirected) {
      window.location.href = res.url;
      return;
    }

    const contentType = res.headers.get('Content-Type') || '';
    let data;
    if (
      contentType.includes('application/json') ||
      contentType.includes('application/problem+json')
    ) {
      try { data = await res.json(); }
      catch (_) { data = null; }
    } else {
      try { data = await res.text(); }
      catch (_) { data = null; }
    }

    return await handleResponse(res.status, data);

  } catch (error) {
    console.error('apiRequest encountered an error:', error);
    throw error;
  }
}

/**
 * Validates a single input element:
 *  - valueMissing → "Field is required."
 *  - tooShort / tooLong → "Field must be between {min} and {max} characters."
 *  - patternMismatch → use data-pattern-msg or "Invalid format."
 *  - typeMismatch (email) → "Please enter a valid email address."
 */
export function validateField(el) {
  const feedback = document.getElementById(el.id + 'Feedback');
  el.classList.remove('is-invalid');
  feedback.textContent = '';

  if (el.validity.valueMissing) {
    el.classList.add('is-invalid');
    feedback.textContent = 'Field is required.';
    return;
  }

  if (el.validity.tooShort || el.validity.tooLong) {
    el.classList.add('is-invalid');
    const min = el.getAttribute('minlength');
    const max = el.getAttribute('maxlength');
    if (el.validity.tooShort && min && !max) {
      feedback.textContent = `Field must be at least ${min} characters.`;
    } else if (el.validity.tooLong && max && !min) {
      feedback.textContent = `Field must be at most ${max} characters.`;
    } else {
      feedback.textContent = `Field must be between ${min} and ${max} characters.`;
    }
    return;
  }

  if (el.validity.patternMismatch) {
    el.classList.add('is-invalid');
    feedback.textContent = el.dataset.patternMsg || 'Invalid format.';
    return;
  }

  if (el.getAttribute('type') === 'email' && el.validity.typeMismatch) {
    el.classList.add('is-invalid');
    feedback.textContent = 'Please enter a valid email address.';
    return;
  }
}

/**
 * Enables/disables a button based on whether *all* inputs are nonempty.
 * @param {HTMLInputElement[]} inputs
 * @param {HTMLButtonElement} button
 */
export function toggleButton(inputs, button) {
  const allFilled = inputs.every(el => el.value.trim());
  button.disabled = !allFilled;
}

/**
 * Measures password strength (0–50 scale).
 */
export function measureStrength(p) {
  let force = 0;
  const regex = /[^A-Za-z0-9]/g;
  const flags = {
    lowerLetters: /[a-z]+/.test(p),
    upperLetters: /[A-Z]+/.test(p),
    numbers: /\d+/.test(p),
    symbols: regex.test(p),
  };
  const passedMatches = Object.values(flags).filter(f => f).length;
  force += 2 * p.length + (p.length >= 10 ? 1 : 0);
  force += passedMatches * 10;
  force = p.length <= 6 ? Math.min(force, 10) : force;
  if (passedMatches === 1) force = Math.min(force, 10);
  if (passedMatches === 2) force = Math.min(force, 20);
  if (passedMatches === 3) force = Math.min(force, 40);
  return force;
}

/**
 * Converts a strength value into a percentage and a Bootstrap bar class.
 */
export function getStrengthSettings(strengthValue) {
  const normalized = Math.min(strengthValue, 50);
  const percent = Math.floor((normalized / 50) * 100);
  let barClass = 'bg-danger';
  if (percent > 80) barClass = 'bg-success';
  else if (percent > 60) barClass = 'bg-primary';
  else if (percent > 40) barClass = 'bg-info';
  else if (percent > 20) barClass = 'bg-warning';
  return { percent, barClass };
}

/**
 * Updates a Bootstrap progress-bar element based on password strength.
 * @param {string} password
 * @param {HTMLElement} strengthBarEl
 */
export function updateStrengthBar(password, strengthBarEl) {
  const strengthValue = measureStrength(password || '');
  const { percent, barClass } = getStrengthSettings(strengthValue);
  strengthBarEl.classList.remove('bg-danger', 'bg-warning', 'bg-info', 'bg-primary', 'bg-success');
  strengthBarEl.classList.add(barClass);
  strengthBarEl.style.width = percent + '%';
  strengthBarEl.setAttribute('aria-valuenow', percent);
}

/**
 * Updates user info in the navbar, dropdown, and localStorage.
 * @param {{username: string, email: string, firstName: string, lastName: string, imageUrl: string, authorities: any}} profile
 */
export function updateProfileDetails(profile) {
  const { username, email, firstName, lastName, imageUrl, authorities} = profile;

  // Navbar username
  const navUserEl = document.getElementById('navUsername');
  if (username && navUserEl) navUserEl.textContent = username;

  // Avatar images
  ['navAvatar', 'dropdownAvatar'].forEach(id => {
    const img = document.getElementById(id);
    if (img && imageUrl) img.src = imageUrl;
  });

  // Dropdown username
  const dropdownLoginEl = document.getElementById('dropdownUsername');
  if (username && dropdownLoginEl) dropdownLoginEl.textContent = username;

  // Dropdown full name
  const dropdownFullNameEl = document.getElementById('dropdownFullName');
  if (firstName && lastName && dropdownFullNameEl) {
    dropdownFullNameEl.textContent = `${firstName} ${lastName}`;
  }

  // Update localStorage
  const stored = JSON.parse(localStorage.getItem('profile') || '{}');
  if (username)    stored.username    = username;
  if (firstName)   stored.firstName   = firstName;
  if (lastName)    stored.lastName    = lastName;
  if (email)       stored.email       = email;
  if (authorities) stored.authorities = authorities;
  if ('protected' in profile) {
    stored.protected = profile.protected;
  }
  if (imageUrl) {
    if (imageUrl !== DEFAULT_AVATAR) {
      stored.imageUrl = imageUrl;
    } else {
      delete stored.imageUrl;
    }
  }
  localStorage.setItem('profile', JSON.stringify(stored));
}
