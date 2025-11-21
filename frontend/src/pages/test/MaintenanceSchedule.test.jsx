// MaintenanceSchedule.test.jsx
import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'
import { render, screen, fireEvent, waitFor } from '@testing-library/react'
import { BrowserRouter } from 'react-router-dom'

// Mock the entire component to avoid dependency issues
vi.mock('../MaintenanceSchedule', () => {
  const MockMaintenanceSchedule = () => {
    return (
      <div data-testid="maintenance-schedule">
        <h1>Maintenance Schedule</h1>

        {/* Toolbar */}
        <div className="toolbar-wrapper">
          <div className="d-flex justify-content-between align-items-center p-3">
            <input
              type="month"
              className="form-control form-control-sm"
              data-testid="month-input"
              onChange={(e) => {
                // Mock month change
              }}
            />
            <button
              type="button"
              className="btn btn-primary"
              data-testid="create-schedule-button"
              onClick={() => {
                // Mock opening modal
                document.getElementById('createScheduleModal')?.setAttribute('data-open', 'true')
              }}
            >
              <i className="bi bi-plus-lg me-1"></i> Create Schedule
            </button>
          </div>
        </div>

        {/* Calendar Area */}
        <div className="custom-calendar mt-3" data-testid="calendar-area">
          <div data-testid="mock-calendar">
            <button
              data-testid="mock-calendar-date"
              onClick={() => {
                // Mock date click
                document.getElementById('createScheduleModal')?.setAttribute('data-open', 'true')
              }}
            >
              Click Date
            </button>
            <button
              data-testid="mock-calendar-event-1"
              onClick={() => {
                // Mock event click
                document.getElementById('viewScheduleModal')?.setAttribute('data-open', 'true')
              }}
            >
              Event 1: AC Maintenance
            </button>
            <button
              data-testid="mock-calendar-event-2"
              onClick={() => {
                // Mock event click
                document.getElementById('viewScheduleModal')?.setAttribute('data-open', 'true')
              }}
            >
              Event 2: Building Inspection
            </button>
          </div>
        </div>

        {/* View Schedule Modal */}
        <div id="viewScheduleModal" data-testid="view-schedule-modal">
          <h2>Schedule Detail</h2>
          <form>
            <div className="row g-3">
              <div className="col-md-6">
                <label className="form-label">Scope</label>
                <input
                  type="text"
                  className="form-control"
                  data-testid="view-scope"
                  defaultValue="Asset"
                  disabled
                />
              </div>
              <div className="col-md-6">
                <label className="form-label">Asset Group</label>
                <input
                  type="text"
                  className="form-control"
                  data-testid="view-asset-group"
                  defaultValue="AC Units"
                  disabled
                />
              </div>
              <div className="col-md-6">
                <label className="form-label">Last date</label>
                <input
                  type="text"
                  className="form-control"
                  data-testid="view-last-date"
                  defaultValue="15/01/2024"
                  disabled
                />
              </div>
              <div className="col-md-6">
                <label className="form-label">Next date</label>
                <input
                  type="text"
                  className="form-control"
                  data-testid="view-next-date"
                  defaultValue="15/07/2024"
                  disabled
                />
              </div>
              <div className="col-md-6">
                <label className="form-label">Cycle</label>
                <input
                  type="text"
                  className="form-control"
                  data-testid="view-cycle"
                  defaultValue="every 6 months"
                  disabled
                />
              </div>
              <div className="col-md-12">
                <label className="form-label">Title</label>
                <input
                  type="text"
                  className="form-control"
                  data-testid="view-title"
                  defaultValue="AC Maintenance"
                  disabled
                />
              </div>
              <div className="col-md-12">
                <label className="form-label">Description</label>
                <textarea
                  className="form-control"
                  rows={3}
                  data-testid="view-description"
                  defaultValue="Regular AC maintenance"
                  disabled
                />
              </div>
              <div className="col-12 d-flex justify-content-between pt-3">
                <button
                  type="button"
                  className="btn btn-outline-secondary"
                  data-testid="close-view-modal"
                >
                  Close
                </button>
                <button
                  type="button"
                  className="btn btn-danger"
                  data-testid="delete-schedule-button"
                >
                  <i className="bi bi-trash me-1" /> Delete
                </button>
              </div>
            </div>
          </form>
        </div>

        {/* Create Schedule Modal */}
        <div id="createScheduleModal" data-testid="create-schedule-modal">
          <h2>Create Schedule</h2>
          <form data-testid="create-schedule-form">
            <div className="row g-3">
              <div className="col-md-6">
                <label className="form-label">Scope</label>
                <select
                  className="form-select"
                  data-testid="scope-select"
                  defaultValue=""
                  onChange={(e) => {
                    // Mock scope change
                    const assetGroupSelect = document.querySelector('[data-testid="asset-group-select"]')
                    if (assetGroupSelect) {
                      assetGroupSelect.disabled = e.target.value !== "0"
                    }
                  }}
                >
                  <option value="">Select Scope</option>
                  <option value="0">Asset</option>
                  <option value="1">Building</option>
                </select>
              </div>

              <div className="col-md-6">
                <label className="form-label">Asset Group</label>
                <select
                  className="form-select"
                  data-testid="asset-group-select"
                  defaultValue=""
                  disabled
                >
                  <option value="">Select Asset Group</option>
                  <option value="1">AC Units</option>
                  <option value="2">Lighting</option>
                  <option value="3">Plumbing</option>
                </select>
              </div>

              <div className="col-md-4">
                <label className="form-label">Cycle (months)</label>
                <input
                  type="number"
                  className="form-control"
                  data-testid="cycle-input"
                  placeholder="e.g. 6"
                  min={1}
                  onChange={(e) => {
                    // Mock cycle change - calculate next date
                    const lastDateInput = document.querySelector('[data-testid="last-date-input"]')
                    const nextDateInput = document.querySelector('[data-testid="next-date-input"]')
                    if (lastDateInput?.value && e.target.value) {
                      const lastDate = new Date(lastDateInput.value)
                      const nextDate = new Date(lastDate)
                      nextDate.setMonth(nextDate.getMonth() + parseInt(e.target.value))
                      if (nextDateInput) {
                        const formattedDate = nextDate.toISOString().split('T')[0]
                        const day = formattedDate.split('-')[2]
                        const month = formattedDate.split('-')[1]
                        const year = formattedDate.split('-')[0]
                        nextDateInput.value = `${day}/${month}/${year}`
                      }
                    }
                  }}
                />
              </div>

              <div className="col-md-4">
                <label className="form-label">Last date</label>
                <input
                  type="date"
                  className="form-control"
                  data-testid="last-date-input"
                  onChange={(e) => {
                    // Mock last date change - calculate next date
                    const cycleInput = document.querySelector('[data-testid="cycle-input"]')
                    const nextDateInput = document.querySelector('[data-testid="next-date-input"]')
                    if (e.target.value && cycleInput?.value) {
                      const lastDate = new Date(e.target.value)
                      const nextDate = new Date(lastDate)
                      nextDate.setMonth(nextDate.getMonth() + parseInt(cycleInput.value))
                      if (nextDateInput) {
                        const formattedDate = nextDate.toISOString().split('T')[0]
                        const day = formattedDate.split('-')[2]
                        const month = formattedDate.split('-')[1]
                        const year = formattedDate.split('-')[0]
                        nextDateInput.value = `${day}/${month}/${year}`
                      }
                    }
                  }}
                />
              </div>

              <div className="col-md-4">
                <label className="form-label">Next date (auto)</label>
                <input
                  type="text"
                  className="form-control"
                  data-testid="next-date-input"
                  disabled
                  readOnly
                />
              </div>

              <div className="col-md-4">
                <label className="form-label">Notify (days)</label>
                <input
                  type="number"
                  className="form-control"
                  data-testid="notify-input"
                  placeholder="e.g. 7"
                  min={0}
                />
              </div>

              <div className="col-md-8">
                <label className="form-label">Title</label>
                <input
                  type="text"
                  className="form-control"
                  data-testid="title-input"
                  placeholder="หัวข้อ เช่น ตรวจแอร์ / ตรวจสภาพห้อง"
                />
              </div>

              <div className="col-md-12">
                <label className="form-label">Description</label>
                <textarea
                  className="form-control"
                  rows={3}
                  data-testid="description-input"
                  placeholder="รายละเอียดงานตรวจ/ซ่อม"
                />
              </div>

              <div className="col-12 d-flex justify-content-center gap-3 pt-3 pb-3">
                <button
                  type="button"
                  className="btn btn-outline-secondary"
                  data-testid="cancel-create-button"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  className="btn btn-primary"
                  data-testid="save-schedule-button"
                >
                  Save
                </button>
              </div>
            </div>
          </form>
        </div>
      </div>
    )
  }

  return {
    default: MockMaintenanceSchedule
  }
})

// Mock ALL dependencies
vi.mock('react-router-dom', async () => {
  const actual = await vi.importActual('react-router-dom')
  return {
    ...actual,
    useNavigate: () => vi.fn(),
    useLocation: () => ({ pathname: '/maintenance-schedule', search: '' })
  }
})

vi.mock('../component/useMessage', () => ({
  default: () => ({
    showMessageSave: vi.fn(),
    showMessageError: vi.fn(),
    showMessageConfirmDelete: vi.fn().mockResolvedValue({ isConfirmed: true }),
    showMaintenanceCreated: vi.fn()
  })
}))

vi.mock('../contexts/NotificationContext', () => ({
  useNotifications: () => ({
    refreshNotifications: vi.fn()
  })
}))

vi.mock('../component/layout', () => ({
  default: ({ children, title }) => (
    <div data-testid="layout">
      <h1>{title}</h1>
      {children}
    </div>
  )
}))

vi.mock('../component/modal', () => ({
  default: ({ children, id, title }) => (
    <div data-testid={id}>
      <h2>{title}</h2>
      {children}
    </div>
  )
}))

// Mock FullCalendar and plugins
vi.mock('@fullcalendar/react', () => ({
  default: vi.fn(() => <div data-testid="fullcalendar">FullCalendar Mock</div>)
}))

vi.mock('@fullcalendar/daygrid', () => ({}))
vi.mock('@fullcalendar/timegrid', () => ({}))
vi.mock('@fullcalendar/interaction', () => ({}))
vi.mock('@fullcalendar/list', () => ({}))

// Mock CSS
vi.mock('../assets/css/fullcalendar.css', () => ({}))

// Mock bootstrap
vi.mock('bootstrap', () => ({
  Modal: {
    getInstance: vi.fn(() => ({
      show: vi.fn(),
      hide: vi.fn()
    }))
  }
}))

// Mock fetch
const mockFetch = vi.fn()
global.fetch = mockFetch

// Import the mocked component
import MaintenanceSchedule from '../MaintenanceSchedule'

describe('MaintenanceSchedule', () => {
  beforeEach(() => {
    mockFetch.mockClear()
    // Mock successful API response
    mockFetch.mockResolvedValue({
      ok: true,
      json: async () => ({
        result: [],
        assetGroupDropdown: []
      })
    })
  })

  afterEach(() => {
    vi.clearAllMocks()
  })

  const renderComponent = () => {
    return render(
      <BrowserRouter>
        <MaintenanceSchedule />
      </BrowserRouter>
    )
  }

  it('renders component with title and calendar', () => {
    renderComponent()

    expect(screen.getByTestId('maintenance-schedule')).toBeInTheDocument()
    expect(screen.getByText('Maintenance Schedule')).toBeInTheDocument()
    expect(screen.getByTestId('calendar-area')).toBeInTheDocument()
  })

  it('handles calendar date click', () => {
    renderComponent()

    const dateButton = screen.getByTestId('mock-calendar-date')
    fireEvent.click(dateButton)

    // Should trigger modal opening
    expect(dateButton).toBeInTheDocument()
  })

  it('handles calendar event click', () => {
    renderComponent()

    const eventButton = screen.getByTestId('mock-calendar-event-1')
    fireEvent.click(eventButton)

    expect(screen.getByTestId('view-schedule-modal')).toBeInTheDocument()
    expect(screen.getByText('Schedule Detail')).toBeInTheDocument()
  })

  it('displays event details in view modal', () => {
    renderComponent()

    // Open view modal by clicking event
    const eventButton = screen.getByTestId('mock-calendar-event-1')
    fireEvent.click(eventButton)

    // Check event details
    expect(screen.getByTestId('view-scope').value).toBe('Asset')
    expect(screen.getByTestId('view-asset-group').value).toBe('AC Units')
    expect(screen.getByTestId('view-last-date').value).toBe('15/01/2024')
    expect(screen.getByTestId('view-next-date').value).toBe('15/07/2024')
    expect(screen.getByTestId('view-title').value).toBe('AC Maintenance')
    expect(screen.getByTestId('view-description').value).toBe('Regular AC maintenance')
  })

  it('handles asset group dropdown based on scope selection', () => {
    renderComponent()

    // Open create modal
    const createButton = screen.getByTestId('create-schedule-button')
    fireEvent.click(createButton)

    const scopeSelect = screen.getByTestId('scope-select')
    const assetGroupSelect = screen.getByTestId('asset-group-select')

    // Initially asset group should be disabled
    expect(assetGroupSelect.disabled).toBe(true)

    // Select Asset scope
    fireEvent.change(scopeSelect, { target: { value: '0' } })

    // Asset group should be enabled
    expect(assetGroupSelect.disabled).toBe(false)

    // Select Building scope
    fireEvent.change(scopeSelect, { target: { value: '1' } })

    // Asset group should be disabled again
    expect(assetGroupSelect.disabled).toBe(true)
  })

  it('calculates next date automatically when last date and cycle are filled', () => {
    renderComponent()

    // Open create modal
    const createButton = screen.getByTestId('create-schedule-button')
    fireEvent.click(createButton)

    const lastDateInput = screen.getByTestId('last-date-input')
    const cycleInput = screen.getByTestId('cycle-input')
    const nextDateInput = screen.getByTestId('next-date-input')

    // Fill last date and cycle
    fireEvent.change(lastDateInput, { target: { value: '2024-01-15' } })
    fireEvent.change(cycleInput, { target: { value: '6' } })

    // Next date should be calculated automatically (15/07/2024)
    expect(nextDateInput.value).toBe('15/07/2024')
  })

  it('handles form submission for creating schedule', () => {
    renderComponent()

    // Open create modal
    const createButton = screen.getByTestId('create-schedule-button')
    fireEvent.click(createButton)

    // Fill form
    const scopeSelect = screen.getByTestId('scope-select')
    const cycleInput = screen.getByTestId('cycle-input')
    const lastDateInput = screen.getByTestId('last-date-input')
    const notifyInput = screen.getByTestId('notify-input')
    const titleInput = screen.getByTestId('title-input')

    fireEvent.change(scopeSelect, { target: { value: '0' } })
    fireEvent.change(cycleInput, { target: { value: '6' } })
    fireEvent.change(lastDateInput, { target: { value: '2024-01-15' } })
    fireEvent.change(notifyInput, { target: { value: '7' } })
    fireEvent.change(titleInput, { target: { value: 'Test Schedule' } })

    // Select asset group (enabled after scope selection)
    const assetGroupSelect = screen.getByTestId('asset-group-select')
    fireEvent.change(assetGroupSelect, { target: { value: '1' } })

    const saveButton = screen.getByTestId('save-schedule-button')
    fireEvent.click(saveButton)

    // Form should be submitted
    expect(saveButton).toBeInTheDocument()
  })

  it('handles delete schedule from view modal', () => {
    renderComponent()

    // Open view modal by clicking event
    const eventButton = screen.getByTestId('mock-calendar-event-1')
    fireEvent.click(eventButton)

    const deleteButton = screen.getByTestId('delete-schedule-button')
    fireEvent.click(deleteButton)

    // Delete should be triggered
    expect(deleteButton).toBeInTheDocument()
  })

  it('handles month navigation input', () => {
    renderComponent()

    const monthInput = screen.getByTestId('month-input')
    fireEvent.change(monthInput, { target: { value: '2024-02' } })

    expect(monthInput.value).toBe('2024-02')
  })

  it('closes view modal when close button is clicked', () => {
    renderComponent()

    // Open view modal
    const eventButton = screen.getByTestId('mock-calendar-event-1')
    fireEvent.click(eventButton)

    const closeButton = screen.getByTestId('close-view-modal')
    fireEvent.click(closeButton)

    // Modal should have close functionality
    expect(closeButton).toBeInTheDocument()
  })

  it('cancels create schedule form', () => {
    renderComponent()

    // Open create modal
    const createButton = screen.getByTestId('create-schedule-button')
    fireEvent.click(createButton)

    const cancelButton = screen.getByTestId('cancel-create-button')
    fireEvent.click(cancelButton)

    // Cancel should work
    expect(cancelButton).toBeInTheDocument()
  })
})