import React, { useEffect, useState } from 'react';
import { toast } from 'react-toastify';
import { Star, MessageSquare, Award, User, Clock } from 'lucide-react';
import { reviewsService } from '../services/reviews.service';
import { dashboardService } from '../services/dashboard.service';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '../components/ui/Card';
import { Skeleton } from '../components/ui/Skeleton';
import { Badge } from '../components/ui/Badge';

export const ReviewsPage = () => {
  const [reviews, setReviews] = useState([]);
  const [stats, setStats] = useState(null);
  const [loading, setLoading] = useState(true);

  const loadReviewsData = async () => {
    try {
      const reviewsRes = await reviewsService.getMyReviews(0, 100);
      const dashboardRes = await dashboardService.getDashboard();

      if (reviewsRes && reviewsRes.success) {
        setReviews(reviewsRes.data.content || []);
      }
      if (dashboardRes && dashboardRes.success) {
        setStats(dashboardRes.data);
      }
    } catch (error) {
      console.error("Failed to load reviews:", error);
      toast.error("Failed to load reviews");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadReviewsData();
  }, []);

  if (loading) {
    return (
      <div className="space-y-6">
        <Skeleton className="h-8 w-48" />
        <Skeleton className="h-32 w-full" />
        <div className="space-y-4">
          <Skeleton className="h-28 w-full" />
          <Skeleton className="h-28 w-full" />
        </div>
      </div>
    );
  }

  const averageRating = stats?.averageRating || 0;
  const roundedStars = Math.round(averageRating);

  return (
    <div className="space-y-6">
      
      {/* Title Header */}
      <div>
        <h1 className="text-3xl font-extrabold text-zinc-900 tracking-tight">Reviews & Ratings</h1>
        <p className="text-sm text-zinc-500">View performance reviews and credentials received from other peers.</p>
      </div>

      {/* Stats Summary Panel */}
      <Card className="bg-zinc-50 border-zinc-200">
        <CardContent className="p-6 flex flex-col sm:flex-row items-center justify-between gap-6">
          <div className="flex items-center space-x-4">
            <div className="p-4 bg-white border rounded-xl flex items-center justify-center shrink-0 shadow-sm">
              <Award className="h-8 w-8 text-zinc-800" />
            </div>
            <div>
              <h2 className="text-lg font-bold text-zinc-900">Exchange Credentials</h2>
              <p className="text-xs text-zinc-500 mt-0.5">Based on completed tutoring sessions.</p>
            </div>
          </div>

          <div className="flex items-center space-x-8">
            {/* Avg rating score */}
            <div className="text-center sm:text-right">
              <span className="text-xs text-zinc-400 font-semibold uppercase tracking-wider block">Average Rating</span>
              <div className="flex items-center sm:justify-end space-x-2 mt-1">
                <span className="text-3xl font-black text-zinc-900">
                  {averageRating ? averageRating.toFixed(1) : '0.0'}
                </span>
                <div className="flex text-amber-500 text-sm">
                  {Array.from({ length: 5 }).map((_, i) => (
                    <span key={i} className={i < roundedStars ? 'text-amber-500' : 'text-zinc-200'}>★</span>
                  ))}
                </div>
              </div>
            </div>

            {/* Total reviews */}
            <div className="text-center sm:text-right">
              <span className="text-xs text-zinc-400 font-semibold uppercase tracking-wider block">Total Reviews</span>
              <span className="text-3xl font-black text-zinc-900 mt-1 block">
                {stats?.totalReviews || 0}
              </span>
            </div>
          </div>
        </CardContent>
      </Card>

      {/* Reviews feed */}
      <div className="space-y-4">
        <h3 className="text-md font-bold text-zinc-900 border-b pb-2">Feedback History</h3>
        
        {reviews.length > 0 ? (
          <div className="space-y-4">
            {reviews.map((r) => (
              <Card key={r.id} className="hover:shadow-sm transition-shadow">
                <CardContent className="p-5 space-y-3">
                  
                  {/* Review Title with Stars */}
                  <div className="flex items-center justify-between flex-wrap gap-2">
                    <div className="flex items-center space-x-2">
                      <div className="h-7 w-7 rounded-full bg-zinc-100 flex items-center justify-center font-bold text-xs text-zinc-600 border border-zinc-200">
                        {r.reviewer.fullName.charAt(0)}
                      </div>
                      <div>
                        <span className="text-sm font-bold text-zinc-900">{r.reviewer.fullName}</span>
                        <span className="text-xs text-zinc-400 ml-1">@{r.reviewer.username}</span>
                      </div>
                    </div>

                    <div className="flex items-center space-x-2">
                      <div className="flex text-amber-500 text-xs mr-1">
                        {Array.from({ length: 5 }).map((_, idx) => (
                          <span key={idx} className={idx < r.rating ? 'text-amber-500' : 'text-zinc-200'}>★</span>
                        ))}
                      </div>
                      <Badge variant="secondary" className="text-xxs">Rating: {r.rating}/5</Badge>
                    </div>
                  </div>

                  {/* Comment */}
                  <p className="text-sm text-zinc-700 leading-relaxed italic bg-zinc-50/50 p-3 rounded-lg border">
                    "{r.comment}"
                  </p>

                  {/* Date log */}
                  <div className="flex items-center space-x-1 text-xxs text-zinc-400 justify-end">
                    <Clock className="h-3 w-3" />
                    <span>
                      Submitted on {new Date(r.createdAt).toLocaleDateString(undefined, {
                        month: 'short',
                        day: 'numeric',
                        year: 'numeric'
                      })}
                    </span>
                  </div>

                </CardContent>
              </Card>
            ))}
          </div>
        ) : (
          <div className="text-center py-20 border border-dashed rounded-xl bg-white text-zinc-400 flex flex-col items-center space-y-2">
            <MessageSquare className="h-8 w-8 text-zinc-300 animate-pulse" />
            <p className="text-sm font-semibold">No feedback reviews received yet.</p>
            <p className="text-xs text-zinc-500">Reviews appear here after completing scheduled sessions.</p>
          </div>
        )}
      </div>

    </div>
  );
};

export default ReviewsPage;
