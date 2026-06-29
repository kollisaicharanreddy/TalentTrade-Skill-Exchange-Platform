import React, { useState } from 'react';
import { cn } from '../../utils/cn';

export const Avatar = ({ className, children }) => {
  return (
    <div className={cn('relative flex h-10 w-10 shrink-0 overflow-hidden rounded-full bg-muted border', className)}>
      {children}
    </div>
  );
};

export const AvatarImage = ({ src, alt, className }) => {
  const [error, setError] = useState(false);

  if (error || !src) return null;

  return (
    <img
      src={src}
      alt={alt}
      onError={() => setError(true)}
      className={cn('aspect-square h-full w-full object-cover', className)}
    />
  );
};

export const AvatarFallback = ({ children, className }) => {
  return (
    <div className={cn('flex h-full w-full items-center justify-center rounded-full bg-muted font-semibold text-sm text-muted-foreground uppercase', className)}>
      {children}
    </div>
  );
};
