import React, { useEffect, useMemo, useState } from "react";
import { db } from "../firebase";
import { ref, onValue } from "firebase/database";
import {
  ResponsiveContainer,
  BarChart,
  Bar,
  XAxis,
  YAxis,
  Tooltip,
  PieChart,
  Pie,
  Cell,
  Legend,
  AreaChart,
  Area
} from "recharts";

const COLORS = ["#4f8cff", "#7b61ff", "#22c55e", "#f59e0b", "#ef4444"];

export default function AdminAnalytics() {

  const [songsMap, setSongsMap] = useState({});

  useEffect(() => {
    const r = ref(db, "songs");
    return onValue(r, (snap) => setSongsMap(snap.val() || {}));
  }, []);

  const songs = useMemo(() => {
    return Object.values(songsMap);
  }, [songsMap]);

  /* ======================
     TOP SONGS
  ====================== */
  const topSongs = useMemo(() => {
    return [...songs]
      .sort((a, b) => (b.viewCount || 0) - (a.viewCount || 0))
      .slice(0, 10);
  }, [songs]);

  /* ======================
     TOP CATEGORIES
  ====================== */
  const topCategories = useMemo(() => {
    const map = {};

    songs.forEach((s) => {
      if (!s.category) return;

      s.category.split(",").forEach((c) => {
        const name = c.trim();
        if (!name) return;
        map[name] = (map[name] || 0) + (s.viewCount || 0);
      });
    });

    return Object.entries(map).map(([name, value]) => ({
      name,
      value
    }));
  }, [songs]);

  /* ======================
     TOTAL STATS
  ====================== */
  const totalViews = songs.reduce(
    (sum, s) => sum + (s.viewCount || 0),
    0
  );

  return (
    <div>
      {/* HEADER */}
      <div className="page-title">
        <h1>Analytics</h1>
      </div>

      {/* STAT CARDS */}
      <div className="pl-grid" style={{ marginBottom: 20 }}>
        <div className="card pl-card">
          <div className="pl-title">Total Songs</div>
          <h2>{songs.length}</h2>
        </div>

        <div className="card pl-card">
          <div className="pl-title">Total Views</div>
          <h2>{totalViews}</h2>
        </div>
      </div>

      {/* CHART GRID */}
      <div className="pl-grid">

        {/* LEFT */}
       <div className="card pl-card">
  <div className="pl-title">🔥 Top 10 Songs</div>

  <ResponsiveContainer width="100%" height={400}>
    <BarChart
      layout="vertical"
      data={topSongs}
      margin={{ top: 30, right: 0, left: 0, bottom: 10 }}
    >
      <XAxis type="number" hide />
      <YAxis
        type="category"
        dataKey="title"
        width={150}
        tick={{ fontSize: 12 }}
      />
      <Tooltip formatter={(v) => [`${v} views`, ""]} />
      <Bar
        dataKey="viewCount"
        fill="#4f8cff"
        radius={[0, 8, 8, 0]}
      />
    </BarChart>
  </ResponsiveContainer>
</div>


        {/* RIGHT */}
        <div className="card pl-card">
          <div className="pl-title">🏷️ Top Categories</div>

          <ResponsiveContainer width="100%" height={400}>
            <PieChart>
              <Pie
                data={topCategories}
                dataKey="value"
                nameKey="name"
                outerRadius={110}
                innerRadius={60}   // làm donut chart cho đẹp
                >

                {topCategories.map((entry, index) => (
                  <Cell
                    key={index}
                    fill={COLORS[index % COLORS.length]}
                  />
                ))}
              </Pie>
              <Tooltip />
              <Legend />
            </PieChart>
          </ResponsiveContainer>
        </div>

      </div>
    </div>
  );
}
