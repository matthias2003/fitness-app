import { secondaryButton } from './styles'

type PaginationProps = {
  page: number
  size: number
  total: number
  onChange: (page: number) => void
}

export function Pagination({ page, size, total, onChange }: PaginationProps) {
  const totalPages = Math.max(1, Math.ceil(total / size))
  const isFirst = page === 0
  const isLast = page >= totalPages - 1

  return (
    <div className="mt-5 flex items-center justify-between gap-3 text-sm">
      <button
        type="button"
        className={`${secondaryButton} disabled:cursor-default disabled:opacity-40`}
        disabled={isFirst}
        onClick={() => onChange(page - 1)}
      >
        Previous
      </button>
      <span className="text-ink">
        Page {page + 1} of {totalPages}
      </span>
      <button
        type="button"
        className={`${secondaryButton} disabled:cursor-default disabled:opacity-40`}
        disabled={isLast}
        onClick={() => onChange(page + 1)}
      >
        Next
      </button>
    </div>
  )
}
